package com.cookandroid.weplog

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dinuscxj.progressbar.CircleProgressBar
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class RecordDetailActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var day_menu : Spinner?= null
    val items = arrayOf("아이템0","아이템1","아이템2","아이템3","아이템4")
    private var mCircleProgressBar : CircleProgressBar?= null // 원형 그래프 (오늘의 스텝 수)

    override fun onDestroy() {
        super.onDestroy()
        supportActionBar!!.show()

    }

    override fun onResume() {
        super.onResume()
        supportActionBar!!.hide()
    }

//    fun calculateDataMatrix() {
//
//        var mCalendar = Calendar.getInstance()
//        var todayDate = (mCalendar.get(Calendar.MONTH)+1).toString() + "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString() + "/" + (mCalendar.get(
//            Calendar.YEAR)).toString()
//        var stepType : Array<Int> = mStepsTrackerDBHelper!!.getStepsByDate(todayDate)
//
//        var walkingSteps = stepType[0]
//        var joggingSteps = stepType[1]
//        var runningSteps = stepType[2]
//        // Calculating total steps
//        var totalStepTaken : Int = walkingSteps + joggingSteps + runningSteps
//        // CircleGraph
//        mCircleProgressBar!!.max = 500
//        mCircleProgressBar!!.progress = totalStepTaken
//        mCircleProgressBar!!.setProgressFormatter(CircleProgressBar.ProgressFormatter { progress, max ->
//            val pattern = "%d Steps"
//            String.format(pattern, progress)
//        })
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record_detail)
        supportActionBar!!.hide()
        database = Firebase.database.reference
        mCircleProgressBar = findViewById(R.id.rec_graph) // 원형 그래프 (오늘의 스텝 수)
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

        initdatabase()

    }

    fun initdatabase() {
        var mCalendar = Calendar.getInstance()
        var todayDate =
            (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(
                Calendar.DAY_OF_MONTH
            )).toString()
        // My top posts by number of stars
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
//                    postSnapshot.
//                    for (snapshot in postSnapshot.children) {
//                        println("postSnapshot"+ snapshot)
//                    }
                    //println("postSnapshot"+ postSnapshot)

                    //var stepInfo  = postSnapshot.child("Pedometer").child(todayDate).getValue(step_info::class.java)

                    //println(stepInfo)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // ...
            }
        })
    }


}

