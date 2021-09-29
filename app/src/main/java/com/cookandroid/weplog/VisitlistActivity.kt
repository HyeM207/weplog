package com.cookandroid.weplog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.record_choice.*

class VisitlistActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_choice)

        val items= mutableListOf<ListViewItem>()
        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_fiber_manual_record_24), "주소"))

        val adapter=ListViewAdapter(items)
        record_choicelist.adapter=adapter

    }


}