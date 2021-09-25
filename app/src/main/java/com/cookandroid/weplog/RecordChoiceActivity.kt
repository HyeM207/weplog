package com.cookandroid.weplog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RecordChoiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_choice)
        supportActionBar!!.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        supportActionBar!!.show()

    }

    override fun onResume() {
        super.onResume()
        supportActionBar!!.hide()
    }
}

