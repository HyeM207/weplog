package com.cookandroid.weplog

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main_history.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HistoryActivity:AppCompatActivity(), bottomsheetFragment.onDataPassListener {

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
    var yearList= ArrayList<String>()
    var dayList= ArrayList<String>()

    //기록 계산 변수
    var distSum=0F
    var timeSum=0
    var plogSum=0


    //리스트뷰
    private var list : ListView?= null
    private lateinit var adapter: HistoryAdapter


    //데이터 저장
    var historyList=ArrayList<History>()

    override fun onDataPass(data: String, data2 : String) {
        Toast.makeText(this, "$data", Toast.LENGTH_SHORT).show()
        choiceMonth = data
        choiceYear = data2
        setHistory(choiceYear, choiceMonth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_history)

        adapter=HistoryAdapter()
        histroy_cardlist.adapter=adapter

        val bottomSheetFragment = bottomsheetFragment(applicationContext)

        history_btnsevenday.setOnClickListener{
            setSevenHis()
        }

        history_btnpermonth.setOnClickListener {
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

        }

        history_btnwhole.setOnClickListener {
            setAllHistory()
        }


        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() +
                "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()

        setToday()


    }

    fun setToday(){
        historyList.clear()
        distSum=0f
        timeSum=0
        plogSum=0

        var currentCalendar=Calendar.getInstance()
        var y = currentCalendar.get(Calendar.YEAR)
        var month = currentCalendar.get(Calendar.MONTH)+1
        var day = currentCalendar.get(Calendar.DAY_OF_MONTH)

        database.child("user/$uid/Pedometer/date/$y/$month").get().addOnSuccessListener {
            if (it.child("$day").hasChildren()){
                var dayData=it.child("$day").children
                dayData=dayData.reversed()
                for (plog in dayData){
                    //플로그 객체 키 값
                    var key=plog.key
                    var History = History()
                    History.year = "$y"
                    History.month = "$month"
                    History.day = "$day"
                    History.time = it.child("$day/$key/record/time").value.toString()
                    History.distance = it.child("$day/$key/record/distance").value.toString()
                    History.startTime = it.child("$day/$key/time/startTime").value.toString()
                    History.endTime = it.child("$day/$key/time/endTime").value.toString()

                    plogSum++

                    var split_time = History.time!!.split(":")
                    timeSum=split_time[0].toInt()*60+split_time[1].toInt()
                    distSum+= History.distance!!.toFloat()

                    historyList.add(History)
                }

                text_km.text= String.format("%.2fkm", distSum)
                text_time.text="${timeSum}분"
                text_plog.text="${plogSum}회"

            }


            adapter.setListData(historyList)
            adapter.notifyDataSetChanged()


        }

    }

    //30일 선택했을 때
    fun setHistory(year: String, month : String){
        //배열 초기화
        historyList.clear()
        dayList.clear()

        distSum=0f
        timeSum=0
        plogSum=0

        database.child("user/$uid/Pedometer/date/$year/$month").get().addOnSuccessListener {
            Log.i("firebase", "check snapshot $it")

            var post = it.children

            for(p in post) {
                var pday = p.key
                Log.i("firebase", "got date value in setChoicedate $pday")
                dayList.add("$pday")
            }
            dayList.reverse()

            for (day in dayList){
                var dayData=it.child("$day").children
                dayData=dayData.reversed()
                for (plog in dayData){
                    //플로그 객체 키 값
                    var key=plog.key
                    var History = History()

                    History.month = month
                    History.day = day
                    History.time = it.child("$day/$key/record/time").value.toString()
                    History.distance = it.child("$day/$key/record/distance").value.toString()
                    History.startTime = it.child("$day/$key/time/startTime").value.toString()
                    History.endTime = it.child("$day/$key/time/endTime").value.toString()

                    plogSum++
                    var split_time = History.time!!.split(":")
                    timeSum=split_time[0].toInt()*60+split_time[1].toInt()
                    distSum+= History.distance!!.toFloat()

                    historyList.add(History)
                    Log.i("firebase", "day : $day, key : $key")
                }

            }
            text_km.text= String.format("%.2fkm", distSum)
            text_time.text="${timeSum}분"
            text_plog.text="${plogSum}회"

            adapter.setListData(historyList)
            adapter.notifyDataSetChanged()

        }
    }

    fun setAllHistory(){
        historyList.clear()
        monthList.clear()
        yearList.clear()
        distSum=0f
        timeSum=0
        plogSum=0

        database.child("user/$uid/Pedometer/date").get().addOnSuccessListener {
            var post = it.children
            for(p in post) {
                var pyear = p.key
                Log.i("firebase", "got date value in setChoicedate $pyear")
                yearList.add("$pyear")
            }
            yearList.reverse()

            //년도
            for (y in yearList){
                post=it.child("$y").children
                //해당 년도의 월들 가져오기
                for (p in post){
                    var pmonth = p.key
                    monthList.add("$pmonth")
                    Log.i("firebase", "got date value in month $pmonth")
                }
                monthList.reverse()


                for (month in monthList){

                    //해당 월에서 일 가져오기
                    var dayPost = it.child("$y/$month").children
                    for (d in dayPost){
                        var pday = d.key
                        Log.i("firebase", "got date value in setallhistory 날짜가져오는 부분 $pday")
                        dayList.add("$pday")
                    }

                    for (day in dayList){
                        var dayData=it.child("$y/$month/$day").children
                        dayData=dayData.reversed()
                        for (plog in dayData){
                            //플로그 객체 키 값
                            var key=plog.key
                            var History = History()
                            History.year = y
                            History.month = month
                            History.day = day
                            History.time = it.child("$y/$month/$day/$key/record/time").value.toString()
                            History.distance = it.child("$y/$month/$day/$key/record/distance").value.toString()
                            History.startTime = it.child("$y/$month/$day/$key/time/startTime").value.toString()
                            History.endTime = it.child("$y/$month/$day/$key/time/endTime").value.toString()

                            plogSum++
                            var split_time = History.time!!.split(":")
                            timeSum=split_time[0].toInt()*60+split_time[1].toInt()
                            distSum+= History.distance!!.toFloat()

                            historyList.add(History)
                            Log.i("firebase", "day : $day, key : $key")
                        }

                    }


                }


            }
            text_km.text= String.format("%.2fkm", distSum)
            text_time.text="${timeSum}분"
            text_plog.text="${plogSum}회"
            adapter.setListData(historyList)
            adapter.notifyDataSetChanged()




        }


    }


    fun setSevenHis(){
        historyList.clear()

        distSum=0f
        timeSum=0
        plogSum=0

        var currentCalendar=Calendar.getInstance()


        database.child("user/$uid/Pedometer/date").get().addOnSuccessListener{

            //7일 전~오늘 날짜 범위 반복문
            for (i in 1..7){
                var y = currentCalendar.get(Calendar.YEAR)
                var month=currentCalendar.get(Calendar.MONTH)+1
                var day=currentCalendar.get(Calendar.DAY_OF_MONTH)
                Log.i("firebase", "7일 범위 날짜 확인 달 : $month, 일 : $day")

                if (it.child("$y/$month/$day").hasChildren()){
                    var dayData=it.child("$y/$month/$day").children
                    dayData=dayData.reversed()
                    for (plog in dayData){
                        //플로그 객체 키 값
                        var key=plog.key
                        Log.i("firebase", "day : $day, key : $key")
                        var History = History()
                        History.year = "$y"
                        History.month = "$month"
                        History.day = "$day"
                        History.time = it.child("$y/$month/$day/$key/record/time").value.toString()
                        History.distance = it.child("$y/$month/$day/$key/record/distance").value.toString()
                        History.startTime = it.child("$y/$month/$day/$key/time/startTime").value.toString()
                        History.endTime = it.child("$y/$month/$day/$key/time/endTime").value.toString()

                        plogSum++

                        var split_time = History.time!!.split(":")
                        timeSum=split_time[0].toInt()*60+split_time[1].toInt()
                        distSum+= History.distance!!.toFloat()

                        historyList.add(History)
                    }
                }

                currentCalendar.add(Calendar.DATE, -1)

            }
            text_km.text= String.format("%.2fkm", distSum)
            text_time.text="${timeSum}분"
            text_plog.text="${plogSum}회"

            adapter.setListData(historyList)
            adapter.notifyDataSetChanged()

        }






    }





}

