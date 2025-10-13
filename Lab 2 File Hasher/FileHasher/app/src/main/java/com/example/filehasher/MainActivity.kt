package com.example.filehasher

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
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    private fun calcHash(filePath: Uri?) {
        Log.i("uri",filePath.toString())
    }
//    val contentResolver = applicationContext.contentResolver
//
//    private fun readTextFromUri(uri: Uri): String {
//        val stringBuilder = StringBuilder()
//        contentResolver.openInputStream(uri)?.use { inputStream ->
//            BufferedReader(InputStreamReader(inputStream)).use { reader ->
//                var line: String? = reader.readLine()
//                while (line != null) {
//                    stringBuilder.append(line)
//                    line = reader.readLine()
//                }
//            }
//        }
//        return stringBuilder.toString()
//    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("deb","hello")
        when {
            intent?.action == Intent.ACTION_SEND -> {
                Log.i("uri","receive")

                val uri = intent.data
                calcHash(uri)

            }

        }
        Log.i("tag","hello")
        enableEdgeToEdge()
        setContent {
            FileHasherTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "hello",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }





        //var hash = calcHash(filePath)
        // return the hash in a "result" intent
        //var resultIntent = Intent()
        //resultIntent.putExtra("hash", hash)
        //  setResult(Activity.RESULT_OK, resultIntent)
        //finish()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FileHasherTheme {
        Greeting("Android")
    }
}