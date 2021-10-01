package com.cookandroid.weplog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class VisitlistActivity:AppCompatActivity() {

    private lateinit var areaList: List<VisitArea>
    private lateinit var adapter: ExpandableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visit_list)

        val recyclerView = findViewById<RecyclerView>(R.id.visitlist_recycler_list)

        areaList = ArrayList()
        areaList = loadData()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpandableAdapter(areaList)
        recyclerView.adapter = adapter

//
//        val items= mutableListOf<ListViewItem>()
//        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_fiber_manual_record_24), "주소"))
//
//        val adapter=ListViewAdapter(items)
//        record_choicelist.adapter=adapter



    }

    private fun loadData(): List<VisitArea> {
        val area = ArrayList<VisitArea>()

        val visitareas = arrayOf("지역1", "지역2")

        for (i in visitareas.indices) {
            val visitarea = VisitArea().apply {
                areaname = visitareas[i]
            }
            area.add(visitarea)
        }
        return area
    }



}