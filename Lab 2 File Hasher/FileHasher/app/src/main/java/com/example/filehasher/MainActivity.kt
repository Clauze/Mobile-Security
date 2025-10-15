package com.example.filehasher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.filehasher.ui.theme.FileHasherTheme
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest

class MainActivity : Activity() {
    private fun calcHash(filePath: Uri): String {
        Log.i("uri",filePath.toString())

        val text = readTextFromUri(filePath)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return hashBytes.fold("") { str, byte -> str + "%02x".format(byte) }
        //Log.i("tagg", "correctly read" + text)
    }
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
//    private fun readTextFromUri(uri: String): String {
//        val bufferedReader: BufferedReader = File(uri).bufferedReader()
//        val inputString = bufferedReader.use { it.readText() }
//        println(inputString)
//        return  inputString
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("tag","hello")

        if (intent?.action == "com.mobiotsec.intent.action.HASHFILE") {
            val uri = intent.data
            if (uri != null) {
                val hash = calcHash(uri)

                val resultIntent = Intent()
                resultIntent.putExtra("hash", hash)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Log.e("HASH", "Nessun URI ricevuto")
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        } else {
            Log.e("HASH", "Intent non riconosciuto")
            setResult(Activity.RESULT_CANCELED)
            finish()
        }





        //var hash = calcHash(filePath)
        // return the hash in a "result" intent
        //var resultIntent = Intent()
        //resultIntent.putExtra("hash", hash)
        //  setResult(Activity.RESULT_OK, resultIntent)
        //finish()
    }
}
