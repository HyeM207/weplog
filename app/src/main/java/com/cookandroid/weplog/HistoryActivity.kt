package com.cookandroid.weplog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.android.synthetic.main.main_history.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.time.Month
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
    var choiceYear=""
    var choiceMonth=""
    var monthList= ArrayList<String>()

    //리스트뷰
    lateinit var listAdapter: ListAdapter
    private var list : ListView?= null

    override fun onDataPass(data: String?) {
        Toast.makeText(this, "$data", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_history)

        var adapter=HistoryAdapter()
        histroy_cardlist.adapter=adapter

        val bottomSheetFragment = bottomsheetFragment(applicationContext)

        history_btnpermonth.setOnClickListener {
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        history_btnwhole.setOnClickListener {
            Toast.makeText(applicationContext, "전체 기록보기", Toast.LENGTH_SHORT).show()
        }


        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() +
                "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()

        database.child("user/$uid/Pedometer/date").get().addOnSuccessListener {
            var post = it.children

            for(p in post){
                var year=p.key //2021 저장됨
                Log.i("firebase", "got date value second $year")
            }
        }
    }

    fun setChoiceDate(year : String, month : String){
        choiceMonth=month
        choiceYear=year
        Log.i("choiceDate", "bottomsheetfragment에서 데이터 전달받음 {$choiceYear, $choiceMonth}")
        database.child("user/$uid/Pedometer/date").get().addOnSuccessListener {
            var post = it.children

            //해당 년도의 월 부분을 가져와서 monthList에 저장
            for(p in post){
                var pmonth=p.children
                for (ps in pmonth){
                    var m=ps.key
                    monthList.add("$m")
                    Log.i("firebase", "got date value in setChoicedate $m")
                }

//                var year=p.key //2021 저장됨
//                var values=p.value
//                Log.i("firebase", "got date value in setChoicedate $values")
            }
        }

    }


}

