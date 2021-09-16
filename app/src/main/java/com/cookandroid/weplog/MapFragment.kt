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
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapReverseGeoCoder
import net.daum.mf.map.api.MapView
import java.util.*

class MapFragment : Fragment() , MapView.CurrentLocationEventListener, MapView.MapViewEventListener{


    private val ACCESS_FINE_LOCATION = 1000     // Request Code

    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val PERMISSIONS_REQUEST_CODE = 100
    private var walkState:Boolean=false

    lateinit var mapPointGeo: MapPoint.GeoCoordinate
    lateinit var address:String



    private val PREF : String = "sharedpref"
    lateinit var map_btnstart : Button

    lateinit var alertDialog : AlertDialog
    lateinit var builder : AlertDialog.Builder

    lateinit var map_btnstop : Button
    lateinit var map_btnend : Button
    lateinit var map_btnGps : Button

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.map, container, false)
        //map_view.setCurrentLocationEventListener(activity.this)

        val prefs : SharedPreferences = requireActivity().getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val editor : SharedPreferences.Editor = prefs.edit() // 데이터 기록을 위한 editor

        map_btnstart = view.findViewById(R.id.map_btnstart)
        map_btnstop = view.findViewById(R.id.map_btnstop)
        map_btnend = view.findViewById(R.id.map_btnend)
        map_btnGps = view.findViewById(R.id.map_btnGps)

//        editor.remove("isStarted")
//        editor.commit()
//        editor.remove("isStoped")
//        editor.commit()

        //Toast.makeText(activity, prefs.getString("isStarted","").toString(), Toast.LENGTH_SHORT).show()


        if (prefs.getString("isStarted","").equals("") ){
            //Toast.makeText(activity,"null", Toast.LENGTH_SHORT).show()
            editor.putString("isStarted", "No")
            editor.putString("isStoped", "No")
            editor.commit() // 필수
        } else if (prefs.getString("isStarted","").equals("Yes")  ){
                //Toast.makeText(activity,"else if 1", Toast.LENGTH_SHORT).show()
                map_btnstart.visibility = View.INVISIBLE
                map_btnstop.visibility = View.VISIBLE
                map_btnend.visibility = View.VISIBLE
            }
            else if (prefs.getString("isStarted","").equals("No") ) {
                //Toast.makeText(activity, "else if 2", Toast.LENGTH_SHORT).show()

                map_btnstart.visibility = View.VISIBLE
                map_btnstop.visibility = View.INVISIBLE
                map_btnend.visibility = View.INVISIBLE
            }


        map_btnstart.setOnClickListener {
            alertDialog()
            map_btnstart.visibility = View.INVISIBLE
            map_btnstop.visibility = View.VISIBLE
            map_btnend.visibility = View.VISIBLE

        }

        map_btnend.setOnClickListener {
            editor.putString("isStarted", "No")
            editor.remove("isStoped")
            editor.commit()

            map_btnstart.visibility = View.VISIBLE
            map_btnstop.visibility = View.INVISIBLE
            map_btnend.visibility = View.INVISIBLE

            map_btnstop.setText("STOP")
            editor.putString("isStoped", "No")
            editor.commit()

            var intent = Intent(activity, Authentication::class.java)
            startActivity(intent)
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
                editor.putString("isStoped", "No")
                editor.commit()
            }
            else if (prefs.getString("isStoped","").equals("No")){
                //Toast.makeText(activity, prefs.getString("isStoped","").toString()+"2", Toast.LENGTH_SHORT).show()
                map_btnstop.setText("RESTART")
                editor.putString("isStoped", "Yes")
                editor.commit()
            }

        }



        map_btnGps.setOnClickListener{
            if (checkLocationService()) {
                // GPS가 켜져있을 경우
                permissionCheck()
            } else {
                // GPS가 꺼져있을 경우
                Toast.makeText(activity, "GPS를 켜주세요", Toast.LENGTH_SHORT).show()
            }
        }

        //start 버튼 누르면 경로그리기 시작
        map_btnstart.setOnClickListener{

        }



        return view

    }




    fun alertDialog(){

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
                editor.putString("isStarted", "Yes")
                editor.commit() // 필수
            })
            builder.setNegativeButton("아니오", DialogInterface.OnClickListener { dialog, which ->
                editor.putString("isStarted", "Yes")
                editor.commit() // 필수
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
    private fun changeWalkState(){
        if(!walkState){
            Toast.makeText(activity, "걸음 시작", Toast.LENGTH_SHORT).show()
            walkState=true

        }
    }




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
        map_view.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

    }

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
        mapPointGeo = currentLocation.mapPointGeoCoord

        address = getCompleteAddressString(requireActivity(), mapPointGeo.latitude, mapPointGeo.longitude)
        map_km.text = address
        map_time.text=mapPointGeo.latitude.toString()
//        mtextView3.setText(address)
        Log.i(
                Chart.LOG_TAG,
                java.lang.String.format(
                        "MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)",
                        mapPointGeo.latitude,
                        mapPointGeo.longitude,
                        accuracyInMeters
                )
        )
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




}