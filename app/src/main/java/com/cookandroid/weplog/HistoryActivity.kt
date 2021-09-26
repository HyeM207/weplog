package com.cookandroid.weplog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.android.synthetic.main.main_history.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*


class HistoryActivity:AppCompatActivity() {

    //db용
    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid

    //날짜
    var mCalendar = Calendar.getInstance()
    lateinit var todayDate:String

    //리스트뷰
    lateinit var listAdapter: ListAdapter
    private var list : ListView?= null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_history)

        var adapter=HistoryAdapter()
        histroy_cardlist.adapter=adapter

        val bottomSheetFragment = bottomsheetFragment(applicationContext)

        history_btnselectmonth.setOnClickListener {
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }


        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() +
                "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()

        database.child("user/$uid/Pedometer/date").get().addOnSuccessListener {
            var post = it.children

            Log.i("firebase", "got date value first $post")
            for(p in post){
                var year=p.key //2021 저장됨
                Log.i("firebase", "got date value second $year")
            }
        }
    }


    fun date_db(){
        var list_item = ArrayList<MonthListViewModel>()
        val user = Firebase.auth.currentUser
        database = Firebase.database.reference
        var mCalendar = Calendar.getInstance()
        var currentyear = (mCalendar.get(Calendar.YEAR)).toString()
        var currentMonth = (mCalendar.get(Calendar.MONTH) + 1).toString()
        var currentday = (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()
        var joinyear : String ?= null
        var joinmonth : String ?= null
        var joinday : String ?= null

        database.child("users").child(user!!.uid).child("joindate").get().addOnSuccessListener {
            println("joindate : " + it.value)
            var joindate = it.value.toString()
            joinyear = joindate.substring(0, joindate.indexOf("/"))
            joinmonth = joindate.substring(5, joindate.lastIndexOf("/"))
            joinday = (joindate.substring(joindate.lastIndexOf("/")+1))
            println( joinyear + joinmonth + joinday)
            if (currentyear.toString() == joinyear.toString()){
                println("if문 : " + currentyear.toString() + joinyear)
                if ( currentMonth.toString() == joinmonth.toString() ){
                    list_item.add(MonthListViewModel(currentyear, currentMonth))
                } else {
                    for (i in joinmonth!!.toInt()..currentMonth.toInt()) {
                        println("i :: " + i)
                        list_item.add(MonthListViewModel(currentyear, i.toString()))
                    }
                }
            } else {
                for(i in joinyear!!.toInt()..currentyear.toInt()){
                    if ( i == currentyear.toInt()) {
                        for ( m in 1..currentMonth.toInt()){
                            list_item.add(MonthListViewModel(i.toString(), m.toString()))
                        }
                    } else if ( i == joinyear!!.toInt()){
                        for ( m in joinmonth!!.toInt()..12){
                            list_item.add(MonthListViewModel(i.toString(), m.toString()))
                        }
                    } else {
                        for ( m in 1..12){
                            list_item.add(MonthListViewModel(i.toString(), m.toString()))
                        }
                    }
                }
            }
            listAdapter = ListAdapter(this, list_item)
            list!!.adapter = listAdapter
            list!!.setOnItemClickListener { parent, view, position, id ->
                val clickedDate = list_item[position]
            }

        }
    }


}

