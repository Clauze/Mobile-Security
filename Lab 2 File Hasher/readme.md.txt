Security threat model:
- Use Intent explicit use signature to select what app should use it(misuse of implicit intent)
- Maybe we can see the log of the app and see the intent, see sensitive data including file and expected intent filter
Protection level, even use implicit intent, we can export something like a component and protect using signature protection.

More info:
- adb sheel
- pm path com.example.victimapp (get location of apk file), goes under data/app, data/data location of data app itself. Random stuff in the middle was not initially available, only data/app, so they add some info to prevent app to auto retrieving info of other app installed in the other device.Pm is a system manager that you always queried, however if you want to locate the other app you will need to ask permission, but if this app is a 3rd party google will ask you why you ask to other app.
- then go do cd /data, you can find log of all installed app, we have victim app and we have 3 different directory,
	we have different storage:
	- internal storage and it is associated to the app, other app cannot access(so cannot list internal storage of an app)
	In the data we have file handle by the challenge, it is located in the private storage of the internal app but should be private, 
	these means that we broke the sandbox, in this case the issue come with victim app that gives read access to read files to externa 	app, you can also specify some flag in URI, that gives read and write accesso to victim, but it is able to read only 1 while victim 	app is running. If app is stopped we are not able to access it, breaking the sandbox.
- sdcard, is the external storage, it exist even if there are no app in the device, initially you have to put in the phone, generic external storage that any app can access, we can find photo, download,...
At first everyone can access it, second step Google acknowledge the issue read and run external storage(dangerous one)
If you go sdcard/android/data, there are some pck, these directory are protected so only app with this name can access it, we have 2 option:
keeping everything private in the internal storage or share I nthe external storage. We have also manage external storage that only sys app can request. We can check if app store something outside, telegram fe store data in the sd card, while some app backup then in the sd card.