package com.cookandroid.weplog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.annotations.NotNull
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
import kotlinx.android.synthetic.main.detail.*
import kotlinx.android.synthetic.main.main_history.*
import kotlinx.android.synthetic.main.record.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


const val WALKING = 0
const val JOGGING = 1
const val RUNNING = 2
const val WALKINGPEAK = 15
const val JOGGINGPEAK = 25
const val RUNNINGPEAK = 30


class RecordActivity : AppCompatActivity(), bottomsheetFragment.onDataPassListener {

    private var barchart: BarChart ?= null
    private var today : TextView ?= null
    private var month_step : TextView ?= null
    private var total_kcal : TextView ?= null
    private var distance : TextView ?= null
    private var rec_plog_data : TextView ?= null
    private var rec_btn : Button ?= null
    private lateinit var database: DatabaseReference
    private var date_list : ListView?= null
    private var listener : ValueEventListener ?= null
    var record_year : String ?= null
    var record_month : String ?= null


    override fun onDataPass(data: String, data2 : String) {
        Toast.makeText(this, "$data", Toast.LENGTH_SHORT).show()
        record_month = data
        record_year = data2
        rec_btn!!.text = record_year.toString() +"년  " + record_month.toString() + "월"
        database.removeEventListener(listener!!)
        db(record_month.toString(), record_year.toString())
    }

    fun db(m : String, y : String){
        val user = Firebase.auth.currentUser
        database = Firebase.database.reference
        var mCalendar = Calendar.getInstance()
        var currentMonth = (mCalendar.get(Calendar.MONTH) + 1).toString()
        var currentYear = (mCalendar.get(Calendar.YEAR)).toString()
        if ( currentMonth != m || currentYear != y ){
            Toast.makeText(this, "다른 ", Toast.LENGTH_SHORT).show()
            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(y).child(m).get().addOnSuccessListener {

                if (it.value.toString() == "null"){
                    barchart!!.isInvisible = true
                    Toast.makeText(this, "아직 플러깅 하지도 않", Toast.LENGTH_SHORT).show()
                    println("아직 플러깅 하지도 않음")
                } else {
                    barchart!!.isInvisible = false
                    Toast.makeText(this, "플러깅 ", Toast.LENGTH_SHORT).show()
                    println("value : " + it.value.toString())
                    calculateData(m, y)
                }
            }.addOnFailureListener{
                barchart!!.isInvisible = true
                Toast.makeText(this, "아직 플러깅 하지도 않", Toast.LENGTH_SHORT).show()
                println("아직 플러깅 하지도 않음")
            }
        } else if ( currentMonth == m && currentYear == y){
            //Toast.makeText(this, "현", Toast.LENGTH_SHORT).show()
            //database.addValueEventListener(listener!!)재
            barchart!!.isInvisible = false
            calculateDataMatrix()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        var actionBar = supportActionBar
        actionBar!!.setTitle("월별 걸음 수")
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        // record 페이지 접근 시 로그인 되어 있는지 확인
        val user = Firebase.auth.currentUser
        println("user : " + user!!.uid)
        if (user == null) {
            Toast.makeText(this, "[Record] user가 null", Toast.LENGTH_SHORT).show()
            var intent = Intent(this, Login::class.java)
            startActivity(intent)

        }

        var mCalendar = Calendar.getInstance()
        var currentMonth = (mCalendar.get(Calendar.MONTH) + 1).toString()
        var currentYear = (mCalendar.get(Calendar.YEAR)).toString()
        record_year = currentYear
        record_month = currentMonth
        var todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "년 " + (mCalendar.get(Calendar.MONTH) + 1).toString() + "월 " + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString() + "일"

        rec_btn = findViewById(R.id.rec_yrmn)
        barchart = findViewById(R.id.rec_graph)
        today = findViewById(R.id.today)
        today!!.text = todayDate
        rec_btn!!.text = currentYear + "년  " + currentMonth + "월"
        month_step = findViewById(R.id.month_step)
        distance = findViewById(R.id.distance_data)
        total_kcal = findViewById(R.id.calory_data)
        rec_plog_data = findViewById(R.id.plog_data)

        calculateDataMatrix()


        val bottomSheetFragment = bottomsheetFragment(applicationContext)

        rec_btn!!.setOnClickListener {
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

        barchart!!.setOnChartValueSelectedListener(object : OnChartValueSelectedListener{
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val x = e!!.x.toInt()
                val y = e!!.y.toInt()
                var step_list = ArrayList<HashMap<String, String>>()

                var intent = Intent(this@RecordActivity, RecordDetailActivity::class.java)
                intent.putExtra("day", x.toString())
                intent.putExtra("month", record_month.toString())
                intent.putExtra("year", record_year.toString())
                intent.putExtra("step", y)
                var plog = 0
                var date = record_year.toString() + "/" + record_month.toString() + "/" + x.toString()
                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(date).get().addOnSuccessListener {
                    plog = it.childrenCount.toInt()
                    intent.putExtra("plog", plog)
                    for(i in it.children){
                        for ( v in i.child("step").children){
                            var t_hashMap = HashMap<String, String>()
                            for ( h in v.children ){
                                t_hashMap.put(h.key.toString(), h.value.toString())
                            }
                            step_list.add(t_hashMap)
                        }
                    }
                    println("step_list : " + step_list)
                }.addOnFailureListener{

                }

                intent.putExtra("step_list", step_list)
                database.child("user").child(user!!.uid).child("Pedometer").child("date").get().addOnSuccessListener {
                    if (it.child(date).value == null){
                        println("null")
                    } else {
                        println("yes")
                        startActivity(intent)
                    }
                }.addOnFailureListener {  }
            }

            override fun onNothingSelected() {

            }
        })
    }

    fun calculateDataMatrix(){

        barchart!!.description.isEnabled = false
        barchart!!.setDrawBarShadow(false)
        barchart!!.setPinchZoom(true)
        barchart!!.setDrawGridBackground(false)
        val xAxis = barchart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        val leftAxis = barchart!!.axisLeft
        val rightAxis = barchart!!.axisRight
        rightAxis.axisMinimum = 0f
        leftAxis.removeAllLimitLines()
        leftAxis.axisMinimum = 0f
        leftAxis.isEnabled = false
        barchart!!.legend.isEnabled = false
        rightAxis.axisMinimum = 0f
        val datevalues: ArrayList<BarEntry> = ArrayList()
        val line2_xlabels = ArrayList<String>()
        val bar_ylabels = ArrayList<String>()
        var day : Int ?= null
        var mCalendar = Calendar.getInstance()
        var month = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString()
        if (((mCalendar.get(Calendar.MONTH)+1) == 4 || (mCalendar.get(Calendar.MONTH)+1) == 6 || (mCalendar.get(Calendar.MONTH)+1) == 9 || (mCalendar.get(Calendar.MONTH)+1) == 11)){
            day = 30
        } else if (((mCalendar.get(Calendar.MONTH)+1) == 2)) {
            day = 28
        } else {
            day = 31
        }
        var uid = Firebase.auth.currentUser!!.uid
        database = Firebase.database.reference
        listener = database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    if (postSnapshot.key == "user") {
                        for (snapshots in postSnapshot.children){
                            if (snapshots.key == Firebase.auth.currentUser!!.uid) {

                                var date: ArrayList<String> = ArrayList()
                                var datecount : ArrayList<Int> = ArrayList()
                                var month_plog = 0
                                var month_kcal = 0f
                                var month_steps : Int = 0
                                var month_dist = 0f
                                var w = 0
                                var j = 0
                                var r = 0
                                for (data in snapshots.child("Pedometer").child("date").child(month).children){
                                    println("dd : " + data.value.toString())
                                    month_plog = month_plog + data.childrenCount.toInt()
                                    var step_count = 0
                                    var dist_count = 0f
                                    for (d in data.children) {
                                        println("ddd : " + d.value.toString()) // 각 객체별 항목
                                        for ( s in d.children ){
                                            if(s.key == "step"){
                                                step_count = step_count + s.childrenCount.toInt()
                                                for ( k in s.children ){
                                                    println("kk : " + k.child("type").value)
                                                    if (k.child("type").value.toString() == "0"){
                                                        w = w + 1

                                                    } else if ( k.child("type").value.toString() == "1") {
                                                        j = j + 1
                                                    } else {
                                                        r = r + 1
                                                    }
                                                }
                                            }
                                            if(s.key == "record"){
                                                dist_count = dist_count + s.child("distance").value.toString().toFloat()
                                            }
                                        }
                                    }
                                    date.add(data.key.toString()) // 일
                                    datecount.add(step_count) // 당일 걸음 수
                                    month_steps = month_steps + step_count // 그 달의 전체 걸음
                                    month_dist = month_dist + dist_count // 그 달의 총 거리
                                    month_kcal = w*0.05f + j*0.1f + r*0.2f
                                }
                                rec_plog_data!!.text = month_plog.toString() + " 회"
                                month_step!!.text = month_steps.toString()
                                distance!!.text = month_dist.toString() + "KM"
                                total_kcal!!.text = String.format("%.2f", month_kcal) + " kcal"
                                for ( i in 1..day){
                                    if ( i.toString() in date){
                                        println("i : " + i.toString())
                                        var t = datecount[date.indexOf(i.toString())]
                                        datevalues.add(BarEntry(i.toFloat(), t.toFloat()))
                                        line2_xlabels.add(i.toString())
                                        bar_ylabels.add(t.toString())
                                    } else {
                                        datevalues.add(BarEntry(i.toFloat(), 0f))
                                        line2_xlabels.add(i.toString())
                                        bar_ylabels.add("0")
                                    }

                                }
                                var set = BarDataSet(datevalues, "")
                                set.setDrawValues(false)
                                set.setColor(Color.parseColor("#1D4028"))
                                var datasets = ArrayList<IBarDataSet>()
                                datasets.add(set)
                                var data = BarData(datasets)
                                barchart!!.data = data
                                barchart!!.axisRight.setGranularity(1.0f)
                                barchart!!.axisRight.setGranularityEnabled(true) // Required to enable granularity
                                barchart!!.xAxis.valueFormatter = (MyValueFormatter3(line2_xlabels))
                                barchart!!.notifyDataSetChanged()
                                barchart!!.invalidate()

                            }
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


    fun calculateData( m : String, y : String){
        //barchart
        barchart!!.description.isEnabled = false
        barchart!!.setDrawBarShadow(false)
        barchart!!.setPinchZoom(true)
        barchart!!.setDrawGridBackground(false)
        val xAxis = barchart!!.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        val leftAxis = barchart!!.axisLeft
        val rightAxis = barchart!!.axisRight
        leftAxis.removeAllLimitLines()
        leftAxis.axisMinimum = 0f
        leftAxis.isEnabled = false
        barchart!!.legend.isEnabled = false

        val datevalues: ArrayList<BarEntry> = ArrayList()
        val line2_xlabels = ArrayList<String>()
        var day : Int ?= null
        var mCalendar = Calendar.getInstance()
        var clickDate = y + "/" + m
        var month = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString()
        if (((mCalendar.get(Calendar.MONTH)+1) == 4 || (mCalendar.get(Calendar.MONTH)+1) == 6 || (mCalendar.get(Calendar.MONTH)+1) == 9 || (mCalendar.get(Calendar.MONTH)+1) == 11)){
            day = 30
        } else if (((mCalendar.get(Calendar.MONTH)+1) == 2)) {
            day = 28
        } else {
            day = 31
        }
        database = Firebase.database.reference
        database.child("user").child(Firebase.auth.currentUser!!.uid).child("Pedometer").child("date").child(clickDate).get().addOnSuccessListener {
            var date: ArrayList<String> = ArrayList()
            var datecount : ArrayList<Int> = ArrayList()
            var month_plog = 0
            var month_kcal = 0f
            var month_steps : Int = 0
            var month_dist = 0f
            var w = 0
            var j = 0
            var r = 0
            month_plog = month_plog + it.childrenCount.toInt()
            for ( data in it.children ){
                var step_count = 0
                var dist_count = 0f
                for (d in data.children) {
                    println("ddd : " + d.value.toString()) // 각 객체별 항목
                    for ( s in d.children ){
                        if(s.key == "step"){
                            step_count = step_count + s.childrenCount.toInt()
                            for ( k in s.children ){
                                println("kk : " + k.child("type").value)
                                if (k.child("type").value.toString() == "0"){
                                    w = w + 1

                                } else if ( k.child("type").value.toString() == "1") {
                                    j = j + 1
                                } else {
                                    r = r + 1
                                }
                            }
                        }
                        if(s.key == "record"){
                            dist_count = dist_count + s.child("distance").value.toString().toFloat()
                        }
                    }
                }
                date.add(data.key.toString()) // 일
                datecount.add(step_count) // 당일 걸음 수
                println("step : " + step_count + "/" + data.key.toString() + "일")
                month_steps = month_steps + step_count // 그 달의 전체 걸음
                month_dist = month_dist + dist_count // 그 달의 총 거리
                month_kcal = w*0.05f + j*0.1f + r*0.2f
            }
            rec_plog_data!!.text = month_plog.toString() + " 회"
            month_step!!.text = month_steps.toString()
            distance!!.text = month_dist.toString() + "KM"
            total_kcal!!.text = String.format("%.2f", month_kcal) + " kcal"
            for ( i in 1..day){
                if ( i.toString() in date){
                    println("i : " + i.toString())
                    var t = datecount[date.indexOf(i.toString())]
                    datevalues.add(BarEntry(i.toFloat(), t.toFloat()))
                    line2_xlabels.add(i.toString())
                } else {
                    datevalues.add(BarEntry(i.toFloat(), 0f))
                    line2_xlabels.add(i.toString())
                }

            }
            var set = BarDataSet(datevalues, "Steps Day")
            set.setDrawValues(false)
            set.setColor(Color.parseColor("#1D4028"))
            var datasets = ArrayList<IBarDataSet>()
            datasets.add(set)
            var data = BarData(datasets)
            barchart!!.data = data
            barchart!!.axisRight.setGranularity(1.0f);
            barchart!!.axisRight.setGranularityEnabled(true); // Required to enable granularity
            barchart!!.xAxis.valueFormatter = (MyValueFormatter3(line2_xlabels))
            barchart!!.notifyDataSetChanged()
            barchart!!.invalidate()
        }.addOnFailureListener {  }

    }
}


class AccelerometerData{
    var value : Double ?= null
    var x : Float ?= null
    var y : Float ?= null
    var z : Float ?= null
    var time : Long ?= null
    var isTruePeak : Boolean = true
}

class StepsTrackerService : Service() {

    companion object{
        const val NOTIFICATION_ID = 10
        const val CHANNEL_ID = "primary_notification_channel"
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    var step_id : String ?= null
    var mSensorManager : SensorManager?= null
    var mStepDetectorSensor : Sensor?= null
    var mAccelerometerSensor : Sensor?= null
    var mAccelerometerListener : AccelerometerListener ?= null
    var mStepDetectorListener : StepDetectorListener ?= null
    private lateinit var database: DatabaseReference

    private fun createNotificationChannel(){ // 백그라운드 서비스라는 걸 알려주는 알림창
        val notificationChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                "The Step Detector notification",
                NotificationManager.IMPORTANCE_HIGH
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = "App Tests"
        val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }


    override fun onCreate() {
        super.onCreate()
        println("onCreate_Service")

        //Toast.makeText(this, step_id, Toast.LENGTH_SHORT).show()

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
//            println("Permission is not granted")
//            requestPermissions(, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_CODE)
////            requestPermissions(CONTEXT,
////                arrayOf(Manifest.permission.REQUESTED_PERMISSION),
////                REQUEST_CODE)
//        }
        database = Firebase.database.reference

        //db 연결 코드 넣기

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("StepsService is running")
                .setContentText("StepsService is running")
                .build()
            Log.d("Test", "start forground")
            startForeground(NOTIFICATION_ID, notification)
        }

        mSensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if(mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mAccelerometerSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            println("가속도계 센서 생성")
            // only create the sensor
        }
        if(mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null){
            mStepDetectorSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mStepDetectorListener = StepDetectorListener()
            mSensorManager!!.registerListener(mStepDetectorListener, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST)
            println("step detector 센서 생성 및 연결")
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        step_id = intent!!.getStringExtra("start")
        Toast.makeText(this, step_id, Toast.LENGTH_SHORT).show()
        return START_STICKY // START_STICKY 상수값을 이용하여 혹시 서비스가 종료되더라도 다시 자동으로 실행되도록 할 수 있다.
    }

    var mScheduledExecutorService : ScheduledExecutorService = Executors.newScheduledThreadPool(4)
    var mScheduledUnregisterAccelerometerTask : ScheduledFuture<Any>?= null
    var mScheduledProcessDataTask : ScheduledFuture<Any>?= null
    var mUnregisterAccelerometerTask : UnregisterAccelerometerTask ?= null
    var mProcessDataTask : ProcessDataTask ?= null
    var isScheduledUnregistered : Boolean = false
    var isAccelerometerRegistered : Boolean = false
    inner class StepDetectorListener() : SensorEventListener {

        var mStepDetector: Int? = 0
        override fun onSensorChanged(event: SensorEvent?) {
            //println("걸음")
            if (event!!.values[0] == 1.0f) {
                mStepDetector = mStepDetector?.plus((event.values[0]).toInt())
                println("걸음 : " + mStepDetector.toString())
            }
            if (!isAccelerometerRegistered && mAccelerometerSensor != null) {
                mAccelerometerListener = AccelerometerListener()
                mSensorManager!!.registerListener(
                    mAccelerometerListener,
                    mAccelerometerSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
                isAccelerometerRegistered = true
                println("가속도계 리스너 등록")
            }
            if (isScheduledUnregistered) {
                //println("mScheduledUnregisterAccelerometerTask.cancel")
                mScheduledUnregisterAccelerometerTask?.cancel(true)

            }
            mScheduledUnregisterAccelerometerTask?.cancel(true)
            mUnregisterAccelerometerTask = UnregisterAccelerometerTask()
            mScheduledExecutorService.schedule(
                mUnregisterAccelerometerTask,
                50000,
                TimeUnit.MILLISECONDS
            ) as ScheduledFuture<Any>?
            isScheduledUnregistered = true
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    inner class UnregisterAccelerometerTask : Runnable{
        override fun run() {
            isAccelerometerRegistered = false
            mSensorManager?.unregisterListener(mAccelerometerListener)
            isScheduledUnregistered = false
            mScheduledProcessDataTask?.cancel(false)
            println("리스너 등록 취소")
        }
    }
    var timeoffsetValue : Long ?= null
    var mAccelerometerDataList : ArrayList<AccelerometerData> = ArrayList<AccelerometerData>()
    var mRawDataList : ArrayList<AccelerometerData> = ArrayList<AccelerometerData>()
    var mAboveThresholdValuesList : ArrayList<AccelerometerData> = ArrayList<AccelerometerData>()
    var mHighestPeakList : ArrayList<AccelerometerData> = ArrayList<AccelerometerData>()


    inner class AccelerometerListener() : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent?) {
            //  println("나는 가속도계다 감지감지감")
            var mAccelerometerData : AccelerometerData = AccelerometerData()
            mAccelerometerData.x = event!!.values[0]
            mAccelerometerData.y = event!!.values[1]
            mAccelerometerData.z = event!!.values[2]
            mAccelerometerData.time = event.timestamp
            mAccelerometerDataList.add(mAccelerometerData)
        }
        init {
            println("init실행")
            mProcessDataTask = ProcessDataTask()
            mScheduledProcessDataTask = mScheduledExecutorService.scheduleWithFixedDelay(mProcessDataTask, 10000, 10000, TimeUnit.MILLISECONDS) as ScheduledFuture<Any>?
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    inner class ProcessDataTask() : Runnable {

        override fun run() { // Copy accelerometer data from main sensor array in separate array for processing
            println("ProcessDataTask run()")
            mRawDataList.addAll(mAccelerometerDataList)
            mAccelerometerDataList.clear()
            println("addAll")
            //Calculating the magnitude (Square root of sum of squares of x,y,z) & converting time from nano seconds from boot time to epoc time
            timeoffsetValue = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            println("timeoffsetvalue : " + timeoffsetValue.toString())
            var dataSize: Int = mRawDataList.size
            println(dataSize)
            for (i in 0..(dataSize - 1)) {
                mRawDataList.get(i).value = Math.sqrt(
                    Math.pow(
                        mRawDataList.get(i).x!!.toDouble(),
                        2.0
                    ) + Math.pow(
                        mRawDataList.get(i).y!!.toDouble(),
                        2.0
                    ) + Math.pow(mRawDataList.get(i).z!!.toDouble(), 2.0)
                )
                mRawDataList.get(i).time =
                    (mRawDataList.get(i).time!! / 1000000L) + timeoffsetValue!!

            }
            //println("step_id" + step_id)
            findHighPeaks()
            removeClosePeaks()
            findStepTypeAndStoreInDB()

            mRawDataList.clear()
            mAboveThresholdValuesList.clear()
            mHighestPeakList.clear()
        }

        fun findHighPeaks() {
            println("findHighPeaks")
            var isAboveMeanLastValueTrue: Boolean = false
            var dataSize: Int = mRawDataList.size
            println(dataSize)
            for (i in 0..(dataSize - 1)) {
                if (mRawDataList.get(i).value!! > WALKINGPEAK) {
                    mAboveThresholdValuesList.add(mRawDataList.get(i))
                    isAboveMeanLastValueTrue = false
                } else {
                    if (!isAboveMeanLastValueTrue && mAboveThresholdValuesList.size > 0) {
                        Collections.sort(mAboveThresholdValuesList, DataSorter())
                        mHighestPeakList.add(mAboveThresholdValuesList.get(mAboveThresholdValuesList.size - 1))
                        mAboveThresholdValuesList.clear()
                    }
                    isAboveMeanLastValueTrue = true
                }
            }
        }

        fun removeClosePeaks() {
            println("removeClosePeaks")
            var dataSize: Int = mHighestPeakList.size
            println(dataSize)
            for (i in 0..(dataSize - 2)) {
                if (mHighestPeakList.get(i).isTruePeak) {
                    if ((mHighestPeakList.get(i + 1).time!! - mHighestPeakList.get(i).time!!) < 400) {
                        if (mHighestPeakList.get(i + 1).value!! > mHighestPeakList.get(i).value!!) {
                            mHighestPeakList.get(i).isTruePeak = false
                        } else {
                            mHighestPeakList.get(i + 1).isTruePeak = false
                        }
                    }
                }
            }
            var count = 0
            for (i in 0..(dataSize - 1)) {
                if (mHighestPeakList.get(i).isTruePeak) {
                    ++count
                }
            }
            println("removeClosePeakCount : " + count.toString())
        }
        //var stepInfo = step_info(RUNNING, mHighestPeakList.get(i).value!!.toInt(), mHighestPeakList.get(i).time!!)
        //database.child("Pedometer").child("date").child(todayDate).push().setValue(stepInfo)
        var counts : Int = 0
        fun findStepTypeAndStoreInDB() { // db에 넣는 부분
            println("findStepTypeAndStoreInDB")
            println("step_id" + step_id)
            var mCalendar = Calendar.getInstance()
            var todayDate =
                (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(
                    Calendar.DAY_OF_MONTH
                )).toString()
            var size = mHighestPeakList.size

            val user = Firebase.auth.currentUser

//            database.child("user").child(user!!.uid).child("Pedometer").get().addOnSuccessListener {
//                //Log.i("firebase", "Got value ${it.value}")
//                println("Got value ${it.value}")
//                //counts = 0
//                counts = it!!.childrenCount.toInt()
//                println("countss : " + counts)
//                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).get().addOnSuccessListener {
//                    //Log.i("firebase", "Got value ${it.value}")
//                    println("Got value ${it.value}")
//                    //counts = 0
//                    counts = it!!.childrenCount.toInt()
//                    println("counts : " + counts)
//                }.addOnFailureListener{
//                    database.child("user").child(user!!.uid).child("Pedometer").child("date").setValue(todayDate)
//                    counts = 0
//                    println(":counts초기")
//                }
//            }.addOnFailureListener{
//                database.child("user").child(user!!.uid).child("Pedometer").child("date").setValue(todayDate)
//                counts = 0
//                println("counts초기")
//            }
            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).get().addOnSuccessListener {
                println("counts초기 아님")
                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").get().addOnSuccessListener {
                    println("counts초기 아님")
                    counts = it!!.childrenCount.toInt()
                    println("counts : " + counts)
                }.addOnFailureListener{
                    println("counts초기")
                }
            }.addOnFailureListener{
                //database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).setValue("step")
                counts = 0
                println("counts초기")
            }
//            println(size)
            for (i in 0..(size - 1)) {
                if (mHighestPeakList.get(i).isTruePeak) {
                    if (mHighestPeakList.get(i).value!! > RUNNINGPEAK) {

                        database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").get().addOnSuccessListener {
                            Log.i("firebase", "Got value ${it.value}")
                            counts = it!!.childrenCount.toInt()
                        }.addOnFailureListener{
                            Log.e("firebase", "Error getting data", it)
                            //counts = 0
                        }
                        if(counts != null){

                            counts = counts!! + 1
                            println("counts : " + counts)
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("type").setValue(RUNNING)
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("peak").setValue(mHighestPeakList.get(i).value!!.toInt())
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("time").setValue(mHighestPeakList.get(i).time!!)
                            println("running" + mHighestPeakList.get(i).value!!.toString())
                        }

                    } else {
                        if (mHighestPeakList.get(i).value!! > JOGGINGPEAK) {
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).get().addOnSuccessListener {
                                Log.i("firebase", "Got value ${it.value}")
                                counts = it!!.childrenCount.toInt()
                            }.addOnFailureListener{
                                Log.e("firebase", "Error getting data", it)
                            }
                            if(counts != null){
                                counts = counts!! + 1
                                println("counts : " + counts)
                                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("type").setValue(JOGGING)
                                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("peak").setValue(mHighestPeakList.get(i).value!!.toInt())
                                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("time").setValue(mHighestPeakList.get(i).time!!)
                                //mStepsTrackerDBHelper!!.createStepsEntry(mHighestPeakList.get(i).time!!, JOGGING, mHighestPeakList.get(i).value!!.toInt())
                                println("jogging" + mHighestPeakList.get(i).value!!.toString())
                            }

                        } else {
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).get().addOnSuccessListener {
                                Log.i("firebase", "Got value ${it.value}")
                                counts = it!!.childrenCount.toInt()
                            }.addOnFailureListener{
                                Log.e("firebase", "Error getting data", it)
                            }
                            if(counts != null){
                                counts = counts!! + 1
                                println("counts : " + counts)
                                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("type").setValue(WALKING)
                                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("peak").setValue(mHighestPeakList.get(i).value!!.toInt())
                                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(step_id.toString()).child("step").child(counts.toString()).child("time").setValue(mHighestPeakList.get(i).time!!)
                                //mStepsTrackerDBHelper!!.createStepsEntry(mHighestPeakList.get(i).time!!, WALKING, mHighestPeakList.get(i).value!!.toInt())
                                println("walking" + mHighestPeakList.get(i).value!!.toString())
                            }

                        }
                    }
                }
            }
        }
    }
    class DataSorter() : Comparator<AccelerometerData> {
        override fun compare(o1: AccelerometerData?, o2: AccelerometerData?): Int {
            var returnVal : Int = 0
            if(o1!!.value!! < o2!!.value!!){
                returnVal = -1
            } else if(o1.value!! > o2.value!!) {
                returnVal = 1
            }
            return returnVal
        }
    }
}


data class step_info (
    val type : Int ?= null,
    val peak : Int ?= null,
    val time : Long ?= null
)
//
//    fun step_info(type: Int, peak: Int, time: Long){
//        this.type = type
//        this.peak = peak
//        this.time = time
//    }

