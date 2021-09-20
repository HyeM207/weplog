package com.cookandroid.weplog

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase



const val WALKING = 0
const val JOGGING = 1
const val RUNNING = 2
const val WALKINGPEAK = 15
const val JOGGINGPEAK = 25
const val RUNNINGPEAK = 30


class RecordActivity : AppCompatActivity() {

    private var barchart: BarChart ?= null
    private var kcal : TextView ?= null
    private var chart_cardview : CardView ?= null
    private var rec_btn : Button ?= null
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.record)

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
//        var todayDate =
//            (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(
//                Calendar.DAY_OF_MONTH
//            )).toString()

        rec_btn = findViewById(R.id.rec_yrmn)
        barchart = findViewById(R.id.rec_graph)
        kcal = findViewById(R.id.rec_kcal)
        chart_cardview = findViewById(R.id.rec_graph_cardview)

        rec_btn!!.text = currentMonth + "월"

        var mStepsAnalysisIntent = Intent(this, StepsTrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(mStepsAnalysisIntent) //안드로이드 8.0이상부터는 startService사용이 어렵다고 함

        } else {
            this.startService(mStepsAnalysisIntent)
        }

        calculateDataMatrix()

        chart_cardview!!.setOnClickListener{
            var intent = Intent(this, RecordDetailActivity::class.java)
            startActivity(intent)
        }
        rec_btn!!.setOnClickListener {
            var intent = Intent(this, RecordChoiceActivity::class.java)
            startActivity(intent)
        }
    }

    fun calculateDataMatrix(){
        //barchart
        barchart!!.description.text = "Step by Date"
        barchart!!.setDrawBarShadow(false)
        barchart!!.setDrawValueAboveBar(true)
        barchart!!.description.isEnabled = true
        barchart!!.setPinchZoom(true)
        barchart!!.setDrawGridBackground(false)
        val leftAxis = barchart!!.axisLeft
        leftAxis.removeAllLimitLines()
        //leftAxis.axisMaximum = 100f
        leftAxis.axisMinimum = 0f
        val datevalues: ArrayList<BarEntry> = ArrayList()
        val line2_xlabels = ArrayList<String>()

        var mCalendar = Calendar.getInstance()
        var todayDate =
            (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(
                Calendar.DAY_OF_MONTH
            )).toString()
        var month = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString()
        database = Firebase.database.reference
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    if (postSnapshot.key == "user") {
                        for (snapshots in postSnapshot.children){
                            if (snapshots.key == Firebase.auth.currentUser!!.uid) {
                                //var day = snapshots.child("Pedometer").child("date").child(month).childrenCount.toString()
                                var date: ArrayList<String> = ArrayList()
                                var datecount : ArrayList<Int> = ArrayList()
                                for (data in snapshots.child("Pedometer").child("date").child(month).children){
                                    date.add(data.key.toString())
                                    datecount.add(data.childrenCount.toInt())
                                    //kcal!!.text = data.childrenCount.toString()
                                }
                                for (i in 0..date.size-1){
                                    var t = datecount[i]
                                    var t2 = date[i]
                                    datevalues.add(BarEntry(i.toFloat(), t.toFloat()))
                                    line2_xlabels.add(t2)
                                }
                                var set = BarDataSet(datevalues, "Steps Day")
                                var datasets = ArrayList<IBarDataSet>()
                                datasets.add(set)
                                var data = BarData(datasets)
                                data.setValueTextSize(6f)
                                data.barWidth = .6f
                                barchart!!.data = data
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

    override fun onBind(intent: Intent?): IBinder? { return null }

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

        fun findStepTypeAndStoreInDB() { // db에 넣는 부분
            println("findStepTypeAndStoreInDB")
            var count : Int = 0
            var mCalendar = Calendar.getInstance()
            var todayDate =
                (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(
                    Calendar.DAY_OF_MONTH
                )).toString()
            var size = mHighestPeakList.size

            val user = Firebase.auth.currentUser

            database.child("user").child(user!!.uid).child("Pedometer").get().addOnSuccessListener {
                Log.i("firebase", "Got value ${it.value}")
                println("Got value ${it.value}")
                database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).get().addOnSuccessListener {
                    Log.i("firebase", "Got value ${it.value}")
                    println("Got value ${it.value}")
                }.addOnFailureListener{
                    database.child("user").child(user!!.uid).child("Pedometer").child("date").setValue(todayDate)
                    count = 0
                }
            }.addOnFailureListener{
                database.child("user").child(user!!.uid).child("Pedometer").child("date").setValue(todayDate)
                count = 0
            }
//            database.child(user!!.uid).child("Pedometer").child("date").child(todayDate).get().addOnSuccessListener {
//                Log.i("firebase", "Got value ${it.value}")
//                println("Got value ${it.value}")
//            }.addOnFailureListener{
//                database.child("Pedometer").child("date").setValue(todayDate)
//                count = 0
//            }
            //database.child("Pedometer").child("date").setValue(todayDate)
            println(size)
            for (i in 0..(size - 1)) {
                if (mHighestPeakList.get(i).isTruePeak) {
                    if (mHighestPeakList.get(i).value!! > RUNNINGPEAK) {
                        count = count + 1
                        database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("type").setValue(RUNNING)
                        database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("peak").setValue(mHighestPeakList.get(i).value!!.toInt())
                        database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("time").setValue(mHighestPeakList.get(i).time!!)
                        println("running" + mHighestPeakList.get(i).value!!.toString())
                    } else {
                        if (mHighestPeakList.get(i).value!! > JOGGINGPEAK) {
                            count = count + 1
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("type").setValue(JOGGING)
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("peak").setValue(mHighestPeakList.get(i).value!!.toInt())
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("time").setValue(mHighestPeakList.get(i).time!!)
                            //mStepsTrackerDBHelper!!.createStepsEntry(mHighestPeakList.get(i).time!!, JOGGING, mHighestPeakList.get(i).value!!.toInt())
                            println("jogging" + mHighestPeakList.get(i).value!!.toString())
                        } else {
                            count = count + 1
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("type").setValue(WALKING)
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("peak").setValue(mHighestPeakList.get(i).value!!.toInt())
                            database.child("user").child(user!!.uid).child("Pedometer").child("date").child(todayDate).child(count.toString()).child("time").setValue(mHighestPeakList.get(i).time!!)
                            //mStepsTrackerDBHelper!!.createStepsEntry(mHighestPeakList.get(i).time!!, WALKING, mHighestPeakList.get(i).value!!.toInt())
                            println("walking" + mHighestPeakList.get(i).value!!.toString())
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

