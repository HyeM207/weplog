package com.cookandroid.weplog

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dinuscxj.progressbar.CircleProgressBar
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyValue_steptype(private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return value.toString()
    }
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        if (value.toInt() >= 0 && value.toInt() <= xValsDateLabel.size - 1) {
            return xValsDateLabel[value.toInt()]
        } else {
            return ("").toString()
        }
    }
}


class MyValueFormatter2(private val xValsDateLabel: ArrayList<String>) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return value.toString()
    }

    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        if (value.toInt() >= 0 && value.toInt() <= xValsDateLabel.size - 1) {
            return xValsDateLabel[value.toInt()]
        } else {
            return ("").toString()
        }
    }
}

class RecordDetailActivity : AppCompatActivity() {
    private var myAdapter : ArrayAdapter<Any> ?= null
    private lateinit var database: DatabaseReference
    private var day_menu : Spinner?= null
    private var test : TextView?= null
    private var totalkcal : TextView ?= null
    var items : ArrayList<Any> ?= null
    var item = arrayOf("1", "2")
    private var mCircleProgressBar : CircleProgressBar?= null // 원형 그래프 (오늘의 스텝 수)
    var mhorizontalBar : HorizontalBarChart?= null // 가로 바 그래프 (step type)
    var mlineChart : LineChart?= null // 시간별 step

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
        database = Firebase.database.reference
        mCircleProgressBar = findViewById(R.id.rec_graph) // 원형 그래프 (오늘의 스텝 수)
        mhorizontalBar = findViewById(R.id.rec_graph_detail)
        mlineChart = findViewById(R.id.rec_graph_detail2)
        day_menu = findViewById(R.id.rec_day_menu)
        test = findViewById(R.id.test
        )
        totalkcal = findViewById(R.id.totalkcal)
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, item)

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
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (postSnapshot in dataSnapshot.children) {
//
//                }
//            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    if (postSnapshot.key == "user"){
                        //println("post : " + postSnapshot)

                        var c = 0
                        for (snapshot in postSnapshot.children){
//                            if (c == 0){
//                                test!!.text = snapshot.toString()
//                                c = c + 1
//                            } else {
//                                totalkcal!!.text = snapshot.toString()
//                            }

                            if (snapshot.key == Firebase.auth.currentUser!!.uid) {
                                //test!!.text = snapshot.child("Pedometer").toString()
                                //val stepInfo = snapshot.child("Pedometer").getValue(step_info::class.java)
                                totalkcal!!.text = snapshot.child("Pedometer").child("date").child(todayDate).toString()
                                // CircleGraph
                                mCircleProgressBar!!.max = 500
                                mCircleProgressBar!!.progress = snapshot.child("Pedometer").child("date").child(todayDate).childrenCount.toInt()
                                mCircleProgressBar!!.setProgressFormatter(CircleProgressBar.ProgressFormatter { progress, max ->
                                    val pattern = "%d Steps"
                                    String.format(pattern, progress)
                                })
                                var stepTime = ArrayList<String>()
                                var stepAcc = ArrayList<Float>()
                                //Calculating total calories burned

                                var w : Int = 0
                                var j = 0
                                var r = 0

                                for (data in snapshot.child("Pedometer").child("date").child((mCalendar.get(Calendar.YEAR)).toString()).child((mCalendar.get(Calendar.MONTH) + 1).toString()).children){
                                    if ( data.key != null ) {
                                        println("data : " + data.key)
                                        items!!.add(data.key!!.toInt())
                                    }
                                }
                                var i : Array<Any> ?= null
                                i = items!!.toArray(arrayOfNulls(items!!.size))
                                //myAdapter = ArrayAdapter<Any>(this, android.R.layout.simple_spinner_dropdown_item, i)


                                //day_menu!!.adapter = myAdapter


//                                day_menu!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//
//                                        //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
//                                        when(position) {
//                                            0   ->  {
//
//                                            }
//                                            1   ->  {
//
//                                            }
//                                            //...
//                                            else -> {
//
//                                            }
//                                        }
//                                    }
//
//                                    override fun onNothingSelected(parent: AdapterView<*>) {
//
//                                    }
//                                }

                                for (data in snapshot.child("Pedometer").child("date").child(todayDate).children){
                                    if ( data != null ) {
                                        if ( data.child("type").value.toString() == "0"){ //walking
                                            w = w + 1
                                            try {
                                                stepTime.add(
                                                    SimpleDateFormat("HH:mm:ss").format(
                                                        data.child("time").value.toString().toLong()
                                                    )
                                                )
                                                stepAcc.add(
                                                    data.child("peak").value.toString().toFloat()
                                                )
                                            } catch ( e : NumberFormatException ) {

                                            }
                                        } else if ( data.child("type").value.toString() == "1" ) { //jogging
                                            j = j + 1
                                            try {
                                            stepTime.add(SimpleDateFormat("HH:mm:ss").format(data.child("time").value.toString().toLong()))
                                            stepAcc.add(data.child("peak").value.toString().toFloat())
                                            } catch ( e : NumberFormatException ) {

                                            }
                                        } else if ( data.child("type").value.toString() == "2" ) { //running
                                            r = r + 1
                                            try {
                                                stepTime.add(SimpleDateFormat("HH:mm:ss").format(data.child("time").value.toString().toLong()))
                                                stepAcc.add(data.child("peak").value.toString().toFloat())
                                            } catch ( e : NumberFormatException ) {

                                            }

                                        }
                                    }
//                                    if ( data.child("type").value.toString() == "0"){ //walking
//                                        w = w + 1
//                                        stepTime.add(SimpleDateFormat("HH:mm:ss").format(data.child("time").value.toString().toLong()))
//                                        stepAcc.add(data.child("peak").value.toString().toFloat())
//                                    } else if ( data.child("type").value.toString() == "1" ) { //jogging
//                                        j = j + 1
//                                        stepTime.add(SimpleDateFormat("HH:mm:ss").format(data.child("time").value.toString().toLong()))
//                                        stepAcc.add(data.child("peak").value.toString().toFloat())
//                                    } else if ( data.child("type").value.toString() == "2" ) { //running
//                                        r = r + 1
//                                        stepTime.add(SimpleDateFormat("HH:mm:ss").format(data.child("time").value.toString().toLong()))
//                                        stepAcc.add(data.child("peak").value.toString().toFloat())
//                                    }
                                    var totalCaloriesBurned : Float = w*0.05f + j*0.1f + r*0.2f
                                    totalkcal!!.text = String.format("%.2f", totalCaloriesBurned) + " kcal"

                                }

                                //line chart
                                mlineChart!!.description.text = "Step by Time(Today)"
                                mlineChart!!.axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                                mlineChart!!.axisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                                mlineChart!!.xAxis.granularity = 1f
                                mlineChart!!.xAxis.isGranularityEnabled = true
                                mlineChart!!.xAxis.setDrawGridLines(false)
                                mlineChart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
                                val ll1 = LimitLine(30f, "RUNNING PEAK")
                                ll1.lineWidth = 1f
                                ll1.enableDashedLine(10f, 10f, 0f)
                                ll1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                                ll1.textSize = 5f
                                val ll2 = LimitLine(25f, "JOGGING PEAK")
                                ll2.lineWidth = 1f
                                ll2.enableDashedLine(10f, 10f, 0f)
                                ll2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                                ll2.textSize = 5f
                                val ll3 = LimitLine(15f, "WALKING PEAK")
                                ll3.lineWidth = 1f
                                ll3.enableDashedLine(10f, 10f, 0f)
                                ll3.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                                ll3.textSize = 5f
                                val leftAxis = mlineChart!!.axisLeft
                                leftAxis.removeAllLimitLines()
                                leftAxis.addLimitLine(ll1)
                                leftAxis.addLimitLine(ll2)
                                leftAxis.addLimitLine(ll3)
                                leftAxis.axisMaximum = 50f
                                leftAxis.axisMinimum = 0f
                                val timevalues: ArrayList<Entry> = ArrayList()
                                val line2_xlabels = ArrayList<String>()
                                for (i in 0..stepTime.size-1){
                                    var t = stepTime[i]
                                    var t2 = stepAcc[i]
                                    println("time : " + t + " , " + t2)
                                    timevalues.add(Entry(i.toFloat(), t2))
                                    line2_xlabels.add(t)
                                }
                                var lineset2 = LineDataSet(timevalues, "Steps Time")
                                lineset2.fillAlpha = 110
                                var line_dataset2 = ArrayList<ILineDataSet>()
                                line_dataset2.add(lineset2)
                                var line_data2 = LineData(line_dataset2)
                                lineset2.setColor(Color.parseColor("#87CEFA"))
                                lineset2.setCircleColor(Color.parseColor("#87CEFA"))
                                lineset2.setDrawFilled(true)
                                lineset2.mode = LineDataSet.Mode.CUBIC_BEZIER
                                lineset2.cubicIntensity = 0.2f
                                lineset2.fillColor = Color.parseColor("#87CEFA")
                                mlineChart!!.data = line_data2
                                mlineChart!!.xAxis.valueFormatter = (MyValueFormatter2(line2_xlabels))
                                mlineChart!!.notifyDataSetChanged()
                                mlineChart!!.invalidate()

                                // horizonchart
                                var type = ArrayList<String>()
                                type.add("Walking")
                                type.add("Jogging")
                                type.add("Running")
                                mhorizontalBar!!.setDrawBarShadow(false)
                                mhorizontalBar!!.setDrawValueAboveBar(true)
                                mhorizontalBar!!.description.isEnabled = true
                                mhorizontalBar!!.setPinchZoom(true)
                                mhorizontalBar!!.setDrawGridBackground(false)
                                mhorizontalBar!!.description.text = "Step Type"
                                var x1 = mhorizontalBar!!.xAxis
                                x1.position = XAxis.XAxisPosition.BOTTOM
                                x1.setDrawAxisLine(true)
                                x1.setDrawGridLines(false)
                                x1.setValueFormatter(MyValue_steptype(type))
                                x1.granularity = 1f
                                var y1 = mhorizontalBar!!.axisLeft
                                y1.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
                                y1.setDrawGridLines(false)
                                y1.setEnabled(false)
                                y1.setAxisMinimum(0f)
                                var yr = mhorizontalBar!!.axisRight
                                yr.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                                yr.setDrawGridLines(false)
                                yr.setAxisMinimum(0f)
                                yr.isEnabled = false
                                var yentries1 = ArrayList<BarEntry>()
                                yentries1.add(BarEntry(0f, w.toFloat()))
                                var yentries2 = ArrayList<BarEntry>()
                                yentries2.add(BarEntry(1f, j.toFloat()))
                                var yentries3 = ArrayList<BarEntry>()
                                yentries3.add(BarEntry(2f, r.toFloat()))
                                var set1 = BarDataSet(yentries1, "Walking")
                                var set2 = BarDataSet(yentries2, "Jogging")
                                var set3 = BarDataSet(yentries3, "Running")
                                set1.setColor(Color.parseColor("#48D1CC"))
                                set2.setColor(Color.parseColor("#7CFC00"))
                                set3.setColor(Color.parseColor("#FFA500"))
                                var datasets = ArrayList<IBarDataSet>()
                                datasets.add(set1)
                                datasets.add(set2)
                                datasets.add(set3)
                                var data = BarData(datasets)
                                data.setValueTextSize(6f)
                                data.barWidth = .6f
                                mhorizontalBar!!.data = data
                                mhorizontalBar!!.notifyDataSetChanged()
                                mhorizontalBar!!.invalidate()
                            }
                        }

                    }

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

//class RecordDetailActivity : AppCompatActivity() {
//
//    private lateinit var database: DatabaseReference
//    private var day_menu : Spinner ?= null
//    private var test : TextView ?= null
//    private var test2 : TextView ?= null
//    val items = arrayOf("아이템0","아이템1","아이템2","아이템3","아이템4")
//    private var mCircleProgressBar : CircleProgressBar ?= null // 원형 그래프 (오늘의 스텝 수)
//    private var mhorizontalBar : HorizontalBarChart ?= null // 가로 바 그래프
//
//    override fun onDestroy() {
//        super.onDestroy()
//        supportActionBar!!.show()
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        supportActionBar!!.hide()
//    }
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.record_detail)
//        supportActionBar!!.hide()
//
//        // record 페이지 접근 시 로그인 되어 있는지 확인
//        val user = Firebase.auth.currentUser
//        println("user : " + user!!.uid)
//        if (user == null) {
//            Toast.makeText(this, "[Record] user가 null", Toast.LENGTH_SHORT).show()
//            var intent = Intent(this, Login::class.java)
//            startActivity(intent)
//
//        }
//
//        database = Firebase.database.reference
//        test = findViewById(R.id.test)
//        test2 = findViewById(R.id.test2)
//        mCircleProgressBar = findViewById(R.id.rec_graph) // 원형 그래프 (오늘의 스텝 수)
//        mhorizontalBar = findViewById(R.id.rec_graph_detail) // 가로 바 그래프
//        day_menu = findViewById(R.id.rec_day_menu)
//        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
//
//        day_menu!!.adapter = myAdapter
//
//        day_menu!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//
//                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
//                when(position) {
//                    0   ->  {
//
//                    }
//                    1   ->  {
//
//                    }
//                    //...
//                    else -> {
//
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//
//            }
//        }
//
//        //initdatabase()
//
//    }
//
//    fun initdatabase() {
//        var mCalendar = Calendar.getInstance()
//        var todayDate =
//            (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(
//                Calendar.DAY_OF_MONTH
//            )).toString()
//        // My top posts by number of stars
//        database.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (postSnapshot in dataSnapshot.children) {
//                    if (postSnapshot.key == "user"){
//                        //println("post : " + postSnapshot)
//
////                        var c = 0
////                        for (snapshot in postSnapshot.children){
////                            if (c == 0){
////                                test!!.text = snapshot.toString()
////                                c = c + 1
////                            } else {
////                                test2!!.text = snapshot.toString()
////                            }
//                            //test!!.text = i.toString()
////                            if (snapshot.key == Firebase.auth.currentUser!!.uid) {
////                                test!!.text = snapshot.child("Pedometer").toString()
////                                //val stepInfo = snapshot.child("Pedometer").getValue(step_info::class.java)
////                                test2!!.text = snapshot.child("Pedometer").child("date").child(todayDate).toString()
////                                // CircleGraph
////                                mCircleProgressBar!!.max = 500
////                                mCircleProgressBar!!.progress = snapshot.child("Pedometer").child("date").child(todayDate).childrenCount.toInt()
////                                mCircleProgressBar!!.setProgressFormatter(CircleProgressBar.ProgressFormatter { progress, max ->
////                                    val pattern = "%d Steps"
////                                    String.format(pattern, progress)
////                                })
////                                var w : Int = 0
////                                var j = 0
////                                var r = 0
////                                for (data in snapshot.child("Pedometer").child("date").child(todayDate).children){
////                                    if ( data.child("type").toString() == "0"){ //walking
////                                        w = w + 1
////                                    } else if ( data.child("type").toString() == "1" ) { //jogging
////                                        j = j + 1
////                                    } else if ( data.child("type").toString() == "2" ) { //running
////                                        r = r + 1
////                                    }
////                                    test2!!.text = data.toString()
////
////                                }
////                            }
////                        }
////
//                    }
//
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Getting Post failed, log a message
//                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//                // ...
//            }
//        })
//    }
//
//
//}
//
