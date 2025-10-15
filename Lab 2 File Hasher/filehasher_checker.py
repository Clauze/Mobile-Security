#!/usr/bin/env python3
"""
Versione migliorata dello script:
- Niente grep (compatibile Windows/Linux)
- Verifica esistenza APK
- Usa subprocess invece di os.system quando possibile
- Rileva apk testOnly tramite `aapt` (se disponibile) e aggiunge -t
- Avvia emulatore e attende il boot
- Logcat gestito in modo non blocking
"""

import argparse
import os
import shutil
import subprocess
import time
from androguard.core.apk import APK

# ---------- utilità ----------
def which(cmd):
    return shutil.which(cmd)

def run(cmd, check=False, capture_output=False, text=True, timeout=None):
    """ Wrapper compatto per subprocess.run """
    return subprocess.run(cmd, check=check, capture_output=capture_output, text=text, timeout=timeout, shell=False)

# ---------- controllo testOnly ----------
def is_test_apk_with_aapt(apk_path):
    """ Usa `aapt dump badging` se disponibile per cercare testOnly=true """
    aapt = which("aapt") or which("aapt.exe")
    if not aapt:
        return None  # non disponibile
    try:
        res = subprocess.check_output([aapt, "dump", "badging", apk_path], text=True, stderr=subprocess.STDOUT)
        return "testOnly=true" in res or "testOnly: true" in res
    except subprocess.CalledProcessError:
        return None

# ---------- adb helpers ----------
def adb(cmd_args, capture_output=False, timeout=None):
    base = ["adb"] + cmd_args
    return run(base, capture_output=capture_output, timeout=timeout)

def wait_for_device_and_boot(timeout=120):
    """Attende adb device e il sys.boot_completed property"""
    start = time.time()
    # aspetta device con adb
    try:
        adb(["wait-for-device"], timeout=timeout)
    except subprocess.TimeoutExpired:
        raise RuntimeError("adb wait-for-device timed out")

    # poi controlla boot_completed
    while True:
        try:
            proc = adb(["shell", "getprop", "sys.boot_completed"], capture_output=True, timeout=5)
            val = proc.stdout.strip() if proc.stdout else ""
            if val == "1":
                return True
        except subprocess.TimeoutExpired:
            pass
        if time.time() - start > timeout:
            raise RuntimeError("Emulator boot timed out")
        time.sleep(1)

# ---------- app management ----------
def package_installed(package_name):
    """Controlla se il package è installato (uso Python per il matching)"""
    proc = adb(["shell", "pm", "list", "packages"], capture_output=True)
    out = proc.stdout or ""
    return any(line.strip().endswith(package_name) or line.strip() == f"package:{package_name}" or f":{package_name}" in line for line in out.splitlines())

def uninstall_package(package_name):
    if package_installed(package_name):
        print(f"[+] Uninstalling {package_name}")
        adb(["uninstall", package_name])

def install_apk(apk_path, allow_test_auto=True, max_retries=3):
    apk_path = os.path.abspath(apk_path)
    if not os.path.exists(apk_path):
        raise FileNotFoundError(f"APK not found: {apk_path}")

    # decide se aggiungere -t
    add_t = False
    if allow_test_auto:
        test_check = is_test_apk_with_aapt(apk_path)
        if test_check is True:
            add_t = True
        elif test_check is None:
            # aapt non disponibile: non decidiamo automaticamente
            add_t = False

    cmd_base = ["adb", "install", "-g"]
    if add_t:
        cmd_base.insert(2, "-t")  # ["adb","install","-t","-g", apk]
    cmd_base.append(apk_path)

    attempt = 0
    while attempt < max_retries:
        attempt += 1
        print(f"[+] Installing (attempt {attempt}): {' '.join(cmd_base)}")
        try:
            proc = run(cmd_base, check=False, capture_output=True, text=True, timeout=120)
            stdout = proc.stdout or ""
            stderr = proc.stderr or ""
            rc = proc.returncode
            # adb install returncode may be 0 on success; check stdout for Success
            if rc == 0 and ("Success" in stdout or "Success" in stderr):
                print("[+] Install succeeded")
                return True
            else:
                print("[!] install failed (rc=%s)" % rc)
                print(stdout)
                print(stderr)
        except subprocess.TimeoutExpired:
            print("[!] install timed out")
        time.sleep(2)  # small backoff
    raise RuntimeError("Failed to install apk after retries")

def launch_app(package_name, main_activity):
    if not main_activity:
        raise ValueError("Main activity not provided")
    activity = f"{package_name}/{main_activity}"
    print(f"[+] Launching: {activity}")
    adb(["shell", "am", "start", "-n", activity])

# ---------- logcat handling ----------
def dump_mobiotsec_logs(output_file="filehasher_logs.txt", filter_tag="MOBIOTSEC", timeout=3):
    """
    Cattura logcat filtrando per tag e salva su file.
    Usa adb logcat -d -s TAG se disponibile (dump istantaneo) altrimenti avvia Popen per timeout secondi.
    """
    # prova logcat -d -s TAG (dump)
    try:
        proc = adb(["logcat", "-d", "-s", filter_tag], capture_output=True, timeout=5)
        with open(output_file, "w", encoding="utf-8") as f:
            f.write(proc.stdout or "")
    except Exception:
        # fallback a Popen con timeout
        p = subprocess.Popen(["adb", "logcat", "-s", filter_tag], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        try:
            out, _ = p.communicate(timeout=timeout)
            with open(output_file, "w", encoding="utf-8") as f:
                f.write(out or "")
        except subprocess.TimeoutExpired:
            p.kill()
            out, _ = p.communicate()
            with open(output_file, "w", encoding="utf-8") as f:
                f.write(out or "")

# ---------- arg parse ----------
def parse_args():
    p = argparse.ArgumentParser()
    p.add_argument("victimapp_apk_path", help="path to the victim app apk file")
    p.add_argument("malapp_apk_path", help="path to the malicious app apk file")
    p.add_argument("--no-auto-test-detect", action="store_true", help="don't auto-detect testOnly to add -t")
    return p.parse_args()

# ---------- main ----------
def main(args):
    # paths assoluti
    victim_path = os.path.abspath(args.victimapp_apk_path)
    mal_path = os.path.abspath(args.malapp_apk_path)

    # controlli preliminari
    for p in (victim_path, mal_path):
        if not os.path.exists(p):
            raise SystemExit(f"APK non trovato: {p}")

    print("[+] Launching emulator (background)...")
    emulator_bin = os.path.expanduser("~/Android/Sdk/emulator/emulator")
    if not os.path.exists(emulator_bin):
        # prova senza expanduser (Windows/altro)
        emulator_bin = which("emulator") or which("emulator.exe") or emulator_bin
    if not emulator_bin or not os.path.exists(emulator_bin):
        print("[!] Emulator binary not found at default location. Assicurati di avere l'emulator nel PATH o modifica il percorso.")
    else:
        # avvia emulatore in background
        subprocess.Popen([emulator_bin, "-avd", "mobiotsec", "-no-audio", "-no-boot-anim", "-accel", "on", "-gpu", "swiftshader_indirect"])

    # aspetta il device/emulator boot
    print("[+] Waiting for emulator/device to boot...")
    try:
        wait_for_device_and_boot(timeout=120)
    except RuntimeError as e:
        print("[!] Attenzione: ", e)
        # puoi decidere di fallire qui o proseguire; decidiamo di proseguire (ma potresti exit)
        # raise

    # creare oggetti androguard solo per estrarre package/activity
    victim_apk_obj = APK(victim_path)
    mal_apk_obj = APK(mal_path)

    victim_pkg = victim_apk_obj.get_package()
    mal_pkg = mal_apk_obj.get_package()
    victim_main = victim_apk_obj.get_main_activity()
    mal_main = mal_apk_obj.get_main_activity()

    # uninstall / install
    uninstall_package(victim_pkg)
    install_apk(victim_path, allow_test_auto=not args.no_auto_test_detect)
    uninstall_package(mal_pkg)
    install_apk(mal_path, allow_test_auto=not args.no_auto_test_detect)

    # launch victim
    launch_app(victim_pkg, victim_main)

    # dump logs
    dump_mobiotsec_logs()

    # mostra a video i log
    with open("filehasher_logs.txt", "r", encoding="utf-8") as f:
        print(f.read())

if __name__ == "__main__":
    main(parse_args())
