package com.cookandroid.weplog

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.github.mikephil.charting.charts.Chart
import kotlinx.android.synthetic.main.map.*
import net.daum.mf.map.api.*
import java.util.*
import android.graphics.Color
import android.location.*
import com.github.mikephil.charting.charts.Chart.LOG_TAG
import com.google.android.gms.location.LocationRequest
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MapFragment : Fragment() , MapView.CurrentLocationEventListener, MapView.MapViewEventListener{


    private val ACCESS_FINE_LOCATION = 1000     // Request Code

    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100

    lateinit var currentPointGeo: MapPoint.GeoCoordinate
    lateinit var address:String


    private val PREF : String = "sharedpref"
    lateinit var map_btnstart : Button

    lateinit var alertDialog : AlertDialog
    lateinit var builder : AlertDialog.Builder

    lateinit var map_btnstop : Button
    lateinit var map_btnend : Button
    lateinit var map_view:MapView

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
    private var timerIsRunning = false
    var sec = 0

    //db용
    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {


        var view = inflater.inflate(R.layout.map, container, false)
        map_view=view.findViewById(R.id.map_view)
        auth = FirebaseAuth.getInstance()

        permissionCheck()
        map_view.setMapViewEventListener(this)
        map_view.setCurrentLocationEventListener(this)

        database = Firebase.database.reference


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

            timerIsRunning = !timerIsRunning
            if (timerIsRunning) startTimer()

            startPoint= MapPoint.mapPointWithGeoCoord(currentPointGeo.latitude, currentPointGeo.longitude)
            makeStartMarker()

            Toast.makeText(activity, prefs.getString("isStarted","").toString(), Toast.LENGTH_SHORT).show()
            startState=true
            Log.i(LOG_TAG, String.format("startbtn click current location (%f,%f)", currentPointGeo.latitude, currentPointGeo.longitude))

//            startAlertDialog()
        }


        map_btnend.setOnClickListener {
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


            map_btnstop.setText("STOP")
            editor.putString("isStoped", "No")
            editor.commit()

            //var intent = Intent(activity, Authentication::class.java)
            //startActivity(intent)
            endAlertDialog()
            endTimer()

            val CurrentUser = FirebaseAuth.getInstance().currentUser
            val uid = CurrentUser?.uid
            var Record = Record()
            //db 저장
            Record.distance= (distanceSum*0.001).toFloat()
            Record.time=sec
            var RecordValues = Record.toMap()
            database.child("test").child("$uid").child("record").setValue(RecordValues)







        }

        map_btnstop.setOnClickListener {
            //Toast.makeText(activity, prefs.getString("isStoped","").toString(), Toast.LENGTH_SHORT).show()


            if (prefs.getString("isStoped","").equals("") ){
                editor.putString("isStoped", "No")
                editor.commit()
            }
            else if (prefs.getString("isStoped","").equals("Yes")){
                // stop 상태일 때 버튼 누름 -> 시작하려고 함
                //Toast.makeText(activity, prefs.getString("isStoped","").toString()+"1", Toast.LENGTH_SHORT).show()
                map_btnstop.setText("STOP")
                startState=true
                editor.putString("isStoped", "No")
                editor.commit()
            }
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
//            val sec=time/100
            sec=time/100
            activity?.runOnUiThread{
                map_time.text="$sec"+"초"
            }

        }
    }

    private fun pauseTimer(){
        timerTask?.cancel()
    }

    private fun endTimer(){
        timerTask?.cancel()
        time=0
        timerIsRunning=false
        map_time.text="0초"
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
                val intent = Intent(activity, QRcodeScanner::class.java)
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



    fun endAlertDialog(){
        try{

            builder = AlertDialog.Builder(requireContext())
            builder.setTitle("[END]")
            //builder.setIcon(R.drawable.tk_app_icon) //팝업창 아이콘 지정
            builder.setMessage("커뮤니티에 인증하시겠습니까?")
            builder.setCancelable(false) //외부 레이아웃 클릭시도 팝업창이 사라지지않게 설정

            builder.setPositiveButton("네", DialogInterface.OnClickListener { dialog, which ->
                var intent = Intent(activity, Authentication::class.java)
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

        map_view.moveCamera(CameraUpdateFactory.newMapPointBounds(mapcenter, padding))

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