package com.cookandroid.weplog

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_history.*
import com.google.firebase.auth.FirebaseAuth
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_history)

        var adapter=HistoryAdapter()
        histroy_cardlist.adapter=adapter

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
}