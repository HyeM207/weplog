package com.cookandroid.weplog

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class RecordDetailActivity : AppCompatActivity() {

    private var day_menu : Spinner?= null
    val items = arrayOf("아이템0","아이템1","아이템2","아이템3","아이템4")

    override fun onDestroy() {
        super.onDestroy()
        supportActionBar!!.show()
    }

    override fun onResume() {
        super.onResume()
        supportActionBar!!.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_detail)
        supportActionBar!!.hide()
        day_menu = findViewById(R.id.rec_day_menu)
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        day_menu!!.adapter = myAdapter

        day_menu!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
                when(position) {
                    0   ->  {

                    }
                    1   ->  {

                    }
                    //...
                    else -> {

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }


}