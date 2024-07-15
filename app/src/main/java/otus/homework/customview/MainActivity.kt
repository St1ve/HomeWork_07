package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.serialization.json.Json

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val payloadRaw = resources.openRawResource(R.raw.payload).bufferedReader().use {
                it.readText()
            }
            val transactions = Json.decodeFromString<List<Transaction>>(payloadRaw)
        }
    }
}