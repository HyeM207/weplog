package com.cookandroid.weplog

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.map.*
import net.daum.mf.map.api.*
import java.text.SimpleDateFormat
import java.util.*

class MapFragment : Fragment() , MapView.CurrentLocationEventListener, MapView.MapViewEventListener{

    // authentication으로 보내는 key 값
    private var pushRefKey : String = ""

    private val ACCESS_FINE_LOCATION = 1000     // Request Code

    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    private var walkState:Boolean=false

    lateinit var currentPointGeo: MapPoint.GeoCoordinate
    lateinit var address:String



    private val PREF : String = "sharedpref"
    lateinit var map_btnstart : Button

    lateinit var alertDialog : AlertDialog
    lateinit var builder : AlertDialog.Builder

    lateinit var map_btnstop : Button
    lateinit var map_btnend : Button
    lateinit var map_btnGps : Button
    lateinit var map_view:MapView

//    lateinit var polyline:MapPolyline
    lateinit var currentPoint:MapPoint
    lateinit var startPoint:MapPoint
    lateinit var endPoint:MapPoint
    var startMarker=MapPOIItem()
    var endMarker=MapPOIItem()

    var polyline:MapPolyline= MapPolyline()
    var startState=false
    lateinit var mapcenter:MapPointBounds


    //이동거리 계산용 변수
    var locationA=Location("point A")
    var locationB=Location("pointB")
    var distance:Float= 0F
    var distanceSum:Float=0F

    //시간 측정용 변수
    private var timerTask:Timer?=null
    private var time = 0
    private val realTimerTask: TimerTask? = null
    private var timerIsRunning = false
    var hour = 0
    var min = 0
    var sec = 0

    //db용
    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid
    private lateinit var pushRef:DatabaseReference

    var mCalendar = Calendar.getInstance()
    lateinit var todayDate:String


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        //pedometer Service
        var mStepsAnalysisIntent = Intent(activity, StepsTrackerService::class.java)
        var view = inflater.inflate(R.layout.map, container, false)
        map_view=view.findViewById(R.id.map_view)
        auth = FirebaseAuth.getInstance()

        permissionCheck()
        map_view.setMapViewEventListener(this)
        map_view.setCurrentLocationEventListener(this)

        database = Firebase.database.reference

        todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() +
                "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()
        //map_view.setCurrentLocationEventListener(activity.this)

        val prefs : SharedPreferences = requireActivity().getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit() // 데이터 기록을 위한 editor

        map_btnstart = view.findViewById(R.id.map_btnstart)
        map_btnstop = view.findViewById(R.id.map_btnstop)
        map_btnend = view.findViewById(R.id.map_btnend)

        map_btnstop.isClickable=false
        map_btnend.isClickable=false

//        editor.remove("isStarted")
//        editor.commit()
//        editor.remove("isStoped")
//        editor.commit()

        Toast.makeText(activity, prefs.getString("isStarted","").toString(), Toast.LENGTH_SHORT).show()
        polyline.lineColor= Color.argb(255,148,234,255)




        if (prefs.getString("isStarted","").equals("") ){
            //Toast.makeText(activity,"null", Toast.LENGTH_SHORT).show()
            editor.putString("isStarted", "No")
            editor.putString("isStoped", "No")
            editor.commit() // 필수
        } else {
            if (prefs.getString("isStarted", "").equals("Yes")) {
                //Toast.makeText(activity, "else if 1", Toast.LENGTH_SHORT).show()
                map_btnstart.visibility = View.INVISIBLE
                map_btnstop.visibility = View.VISIBLE
                map_btnend.visibility = View.VISIBLE
            }
            if (prefs.getString("isStarted", "").equals("No")) {
                //Toast.makeText(activity, "else if 2", Toast.LENGTH_SHORT).show()

                map_btnstart.visibility = View.VISIBLE
                map_btnstop.visibility = View.INVISIBLE
                map_btnend.visibility = View.INVISIBLE
            }
        }

        map_btnstart.setOnClickListener {
            Toast.makeText(activity, "start click listener", Toast.LENGTH_SHORT).show()
            editor.putString("isStarted", "Yes")
            editor.commit()

            map_btnstart.visibility = View.INVISIBLE
            map_btnstop.visibility = View.VISIBLE
            map_btnend.visibility = View.VISIBLE

            var now=System.currentTimeMillis()
            var date=Date(now)
            var startdateFormat=SimpleDateFormat("k:mm")
            var startTimeString=startdateFormat.format(date)



            timerIsRunning = !timerIsRunning
            if (timerIsRunning) startTimer()

            startPoint= MapPoint.mapPointWithGeoCoord(currentPointGeo.latitude, currentPointGeo.longitude)
            makeStartMarker()

            Toast.makeText(activity, prefs.getString("isStarted","").toString(), Toast.LENGTH_SHORT).show()
            startState=true
            Log.i(LOG_TAG, String.format("startbtn click current location (%f,%f)", currentPointGeo.latitude, currentPointGeo.longitude))

            pushRef=database.child("user/$uid/Pedometer/date").child(todayDate).push()
            //pushRef.child("step/type").setValue("0")
            pushRef.child("time/startTime").setValue("$startTimeString")
            println("pp : " + pushRef.key)

            //pedometer service start
            mStepsAnalysisIntent.putExtra("start", pushRef.key.toString())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(mStepsAnalysisIntent) //안드로이드 8.0이상부터는 startService사용이 어렵다고 함
            } else {
                requireActivity().startService(mStepsAnalysisIntent)
            }

//            startAlertDialog()
        }


        map_btnend.setOnClickListener {

            //pedometer Service 중단
            requireActivity().stopService(mStepsAnalysisIntent)

            editor.putString("isStarted", "No")
            editor.remove("isStoped")
            editor.commit()
            Log.i(LOG_TAG, String.format("btnend click : currentpoint (%f, %f)", currentPointGeo.latitude, currentPointGeo.longitude))

            endPoint= MapPoint.mapPointWithGeoCoord(currentPointGeo.latitude, currentPointGeo.longitude)
//            makeEndMarker()

            map_btnstart.visibility = View.VISIBLE
            map_btnstop.visibility = View.INVISIBLE
            map_btnend.visibility = View.INVISIBLE
            startState=false
            endTimer()
            map_view.removePOIItem(startMarker)


            map_btnstop.setText("STOP")
            editor.putString("isStoped", "No")
            editor.commit()

            //var intent = Intent(activity, Authentication::class.java)
            //startActivity(intent)

            endAlertDialog()


            var now=System.currentTimeMillis()
            var date=Date(now)
            var endDateFormat=SimpleDateFormat("k:mm")
            var endTimeString=endDateFormat.format(date)

            var Record = Record()
            //db 저장

//            Record.distance= (distanceSum*0.001).toFloat()
            Record.distance = String.format("%.2f", distanceSum*0.001)
            Record.time=String.format("%02d:%02d:%02d", hour, min, sec)
            var RecordValues = Record.toMap()
            Log.i("firebase", "time : $sec")
            pushRef.child("record").setValue(RecordValues)
            pushRef.child("time/endTime").setValue("$endTimeString")
            var plogkey=pushRef.key
//            database.child("user/$uid/visit/서울특별시/노원구/월계2동 845-13/count").setValue("0")
            database.child("user/$uid/visit/서울특별시/도봉구/쌍문1동 삼양로144길/count").setValue("0")
            database.child("user/$uid/visit/서울특별시/도봉구/방학3 501-9/count").setValue("0")
            database.child("user/$uid/visit/경기도/고양시/덕양구 흥도동/count").setValue("0")



            map_km.text="0.00km"
            time=0
            hour=0
            min=0
            sec=0
            map_time.text=String.format("%02d:%02d:%02d", hour, min, sec)



        }

        map_btnstop.setOnClickListener {
            //Toast.makeText(activity, prefs.getString("isStoped","").toString(), Toast.LENGTH_SHORT).show()


            if (prefs.getString("isStoped","").equals("") ){
                editor.putString("isStoped", "No")
                editor.commit()
            }
            //restart
            else if (prefs.getString("isStoped","").equals("Yes")){
                // stop 상태일 때 버튼 누름 -> 시작하려고 함
                //Toast.makeText(activity, prefs.getString("isStoped","").toString()+"1", Toast.LENGTH_SHORT).show()
                map_btnstop.setText("STOP")
                startState=true
                timerIsRunning = !timerIsRunning
                if(timerIsRunning) startTimer()
                editor.putString("isStoped", "No")
                editor.commit()
            }
            //stop 눌렀을 때
            else if (prefs.getString("isStoped","").equals("No")){
                //Toast.makeText(activity, prefs.getString("isStoped","").toString()+"2", Toast.LENGTH_SHORT).show()
                map_btnstop.setText("RESTART")
                startState=false
                timerIsRunning = !timerIsRunning
                if(!timerIsRunning) pauseTimer()
                editor.putString("isStoped", "Yes")
                editor.commit()
            }

        }

        return view

    }

    private fun makeStartMarker(){
        startMarker.itemName="시작 지점"
        startMarker.tag=0
        startMarker.markerType=MapPOIItem.MarkerType.BluePin
        startMarker.selectedMarkerType=MapPOIItem.MarkerType.RedPin

        if (this::startPoint.isInitialized){
            startMarker.mapPoint=startPoint
            map_view.addPOIItem(startMarker)

        }

    }

    private fun makeEndMarker(){
        endMarker.itemName="end point"
        endMarker.tag=1
        endMarker.markerType=MapPOIItem.MarkerType.YellowPin
        endMarker.selectedMarkerType=MapPOIItem.MarkerType.BluePin
        if (this::endPoint.isInitialized){
//            endMarker.mapPoint=endPoint
            endMarker.mapPoint= MapPoint.mapPointWithGeoCoord(38.637043,127.067093)
            map_view.addPOIItem(endMarker)
        }
    }


    private fun startTimer(){
        timerTask=kotlin.concurrent.timer(period = 10){
            time++
            hour=time/100/3600
            min=(time/100/60)%60
            sec=(time/100)%60
            activity?.runOnUiThread{
                map_time.text=String.format("%02d:%02d:%02d", hour, min, sec)
//                map_time.text="$sec"
            }

        }
    }

    private fun pauseTimer(){
        timerTask?.cancel()
    }

    private fun endTimer(){
        timerTask?.cancel()
//        time=0
//        hour=0
//        min=0
//        sec=0
//        map_time.text=String.format("%02d:%02d:%02d", hour, min, sec)
//        map_time.text="00:00:00"
    }

    fun startAlertDialog(){

        val prefs : SharedPreferences = requireActivity().getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit()

        try{

            var str_buttonOK = "확인"
            var str_buttonNO = "취소"
            var str_buttonNature = "이동"

            builder = AlertDialog.Builder(requireContext())
            builder.setTitle("[START]")
            //builder.setIcon(R.drawable.tk_app_icon) //팝업창 아이콘 지정
            builder.setMessage("쓰레기봉투의 QR을 촬영하시겠습니까?")
            builder.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정

            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->
                database = Firebase.database.reference
                val user = Firebase.auth.currentUser

                // 하루에 한 번 인증 제한
                database.child("users").child(user?.uid.toString()).get().addOnSuccessListener {

                    var mCalendar = Calendar.getInstance()
                    val todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()
                    val lastAuth = it.child("lastAuth").value.toString()

                    Log.e ("auth",todayDate +", "+lastAuth+".")
                    if (! lastAuth.equals(todayDate)){ // 인증 가능함
                        Log.e("auth","equal하지 않음")
                        var intent = Intent(activity, Authentication::class.java)
                        startActivity(intent)
                    }
                    else{ // 인증 못 함
                        Log.e("auth","equal함")
                        Toast.makeText(requireContext(),"오늘 이미 인증하셨습니다. (인증 일일 1회 제한)",Toast.LENGTH_SHORT).show()
                        var intent = Intent(activity, NavigationActivity::class.java)
                        startActivity(intent)
                    }
                }


            })
            builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, which ->
            })

            alertDialog = builder.create()

            try {
                alertDialog.show()
            }
            catch (e : Exception){
                e.printStackTrace()
            }
        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }



    fun endAlertDialog(){
        try{

            builder = AlertDialog.Builder(requireContext())
            builder.setTitle("[END]")
            //builder.setIcon(R.drawable.tk_app_icon) //팝업창 아이콘 지정
            builder.setMessage("커뮤니티에 인증하시겠습니까?")
            builder.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정

            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->

                var intent = Intent(activity, Authentication::class.java)
                intent.putExtra("pushRefKey",pushRefKey)
                Log.i("firebase", "check pushrefkey in mapfragment : $pushRefKey")
                startActivity(intent)
            })
            builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, which ->
            })

            alertDialog = builder.create()

            try {
                alertDialog.show()
            }
            catch (e : Exception){
                e.printStackTrace()
            }
        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }


    //경로 그리기
    fun drawLine(){
        polyline.lineColor= Color.argb(255,148,234,255)
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(currentPointGeo.latitude, currentPointGeo.longitude))
        map_view.addPolyline(polyline)

        var mapcenter=MapPointBounds(polyline.mapPoints)
        var padding=100

//        map_view.moveCamera(CameraUpdateFactory.newMapPointBounds(mapcenter, padding))

        //이동거리 계산
        if (locationB.latitude > 0){
            distance=locationA.distanceTo(locationB)
            Log.i(LOG_TAG, String.format("Map distance between A B %f", distance))
            distanceSum+=distance
            map_km.text= String.format("%.2f km", distanceSum*0.001)
        }




    }


    //권한 확인 함수
    private fun permissionCheck() {
        val preference = activity?.getPreferences(AppCompatActivity.MODE_PRIVATE)
        val isFirstCheck = preference?.getBoolean("isFirstPermissionCheck", true)
        if (ContextCompat.checkSelfPermission(requireContext() , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 상태
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 권한 거절 (다시 한 번 물어봄)
                val builder = AlertDialog.Builder(requireContext() )
                builder.setMessage("현재 위치를 확인하시려면 위치 권한을 허용해주세요.")
                builder.setPositiveButton("확인") { dialog, which ->
                    ActivityCompat.requestPermissions(requireActivity() , arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
                }
                builder.setNegativeButton("취소") { dialog, which ->

                }
                builder.show()
            } else {
                if (isFirstCheck == true) {
                    // 최초 권한 요청
                    preference?.edit()?.putBoolean("isFirstPermissionCheck", false)?.apply()
                    ActivityCompat.requestPermissions( requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION)
                } else {
                    // 다시 묻지 않음 클릭 (앱 정보 화면으로 이동)
                    val builder = AlertDialog.Builder(requireContext() )
                    builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
                    builder.setPositiveButton("설정으로 이동") { dialog, which ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+requireActivity().getPackageName().toString()))
                        startActivity(intent)
                    }
                    builder.setNegativeButton("취소") { dialog, which ->

                    }
                    builder.show()
                }
            }
        } else {
            // 권한이 있는 상태
            startTracking()
        }
    }

    // 권한 요청 후 행동
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 요청 후 승인됨 (추적 시작)
                Toast.makeText(requireContext(), "위치 권한이 승인되었습니다", Toast.LENGTH_SHORT).show()
                startTracking()
            } else {
                // 권한 요청 후 거절됨 (다시 요청 or 토스트)
                Toast.makeText(requireContext(), "위치 권한이 거절되었습니다", Toast.LENGTH_SHORT).show()
                permissionCheck()
            }
        }
    }

    // GPS가 켜져있는지 확인
    private fun checkLocationService(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun startTracking(){
//        map_view.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
        map_view.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
    }

    //주소 좌표를 한글 주소로 반환
    private fun getCompleteAddressString(context: Context?, LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.getMaxAddressLineIndex()) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("MyCurrentloctionaddress", strReturnedAddress.toString())
            } else {
                Log.w("MyCurrentloctionaddress", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("MyCurrentloctionaddress", "Canont get Address!")
        }

        // "대한민국 " 글자 지워버림
        strAdd = strAdd.substring(5)
        return strAdd
    }

    override fun onCurrentLocationUpdate(mapView: MapView?, currentLocation: MapPoint, accuracyInMeters: Float) {

        currentPointGeo = currentLocation.mapPointGeoCoord
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", currentPointGeo.latitude, currentPointGeo.longitude, accuracyInMeters))
//        startLatLng=MapCoordLatLng(currentPointGeo.latitude, currentPointGeo.longitude)

        //현재 좌표를 한글 주소로 변경한 것
        address = getCompleteAddressString(requireActivity(), currentPointGeo.latitude, currentPointGeo.longitude)
//        map_time.text=currentPointGeo.latitude.toString()
        map_km.text= String.format("%.2f km", distanceSum*0.001)
        //시작 버튼을 누른 상태일 때
        if(startState){
            locationA.latitude=currentPointGeo.latitude
            locationA.longitude=currentPointGeo.longitude
            //선 그리기
            drawLine()
            locationB.latitude=currentPointGeo.latitude
            locationB.longitude=currentPointGeo.longitude
//            polyline.addPoint(MapPoint.mapPointWithGeoCoord(currentPointGeo.latitude, currentPointGeo.longitude))
//            map_view.addPolyline(polyline)
//
//            var mapcenter=MapPointBounds(polyline.mapPoints)
//            var padding=100
//
//            map_view.moveCamera(CameraUpdateFactory.newMapPointBounds(mapcenter, padding))
        }


    }


    override fun onCurrentLocationDeviceHeadingUpdate(mapView: MapView?, v: Float) {}

    override fun onCurrentLocationUpdateFailed(mapView: MapView?) {}

    override fun onCurrentLocationUpdateCancelled(mapView: MapView?) {}

    fun onReverseGeoCoderFoundAddress(mapReverseGeoCoder: MapReverseGeoCoder, s: String?) {
        mapReverseGeoCoder.toString()
        if (s != null) {
            onFinishReverseGeoCoding(s)
        }
    }

    override fun onMapViewInitialized(mapView: MapView?) {}

    override fun onMapViewCenterPointMoved(mapView: MapView?, mapPoint: MapPoint?) {}

    override fun onMapViewZoomLevelChanged(mapView: MapView?, i: Int) {}

    override fun onMapViewSingleTapped(mapView: MapView?, mapPoint: MapPoint?) {
        //검색창켜져있을때 맵클릭하면 검색창 사라지게함
    }

    override fun onMapViewDoubleTapped(mapView: MapView?, mapPoint: MapPoint?) {}

    override fun onMapViewLongPressed(mapView: MapView?, mapPoint: MapPoint?) {}

    override fun onMapViewDragStarted(mapView: MapView?, mapPoint: MapPoint?) {}

    override fun onMapViewDragEnded(mapView: MapView?, mapPoint: MapPoint?) {}

    override fun onMapViewMoveFinished(mapView: MapView?, mapPoint: MapPoint?) {}

    fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, i: Int, s: String?) {}

    fun onPOIItemSelected(mapView: MapView?, mapPOIItem: MapPOIItem?) {}

    fun onReverseGeoCoderFailedToFindAddress(mapReverseGeoCoder: MapReverseGeoCoder?) {
        onFinishReverseGeoCoding("Fail")
    }

    private fun onFinishReverseGeoCoding(result: String) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }

    companion object {
        //현재 위치 추적
        private fun startTracking(mapFragment: MapFragment){
            mapFragment.map_view.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading
            mapFragment.map_btnstart.visibility = View.INVISIBLE
            mapFragment.map_btnstop.visibility = View.VISIBLE
            mapFragment.map_btnend.visibility = View.VISIBLE
            mapFragment.map_btnstop.isClickable=true
            mapFragment.map_btnend.isClickable=true
    //        drawLine()
    
        }
    }


}