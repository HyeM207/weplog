package com.cookandroid.weplog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_visit.*
import net.daum.mf.map.api.*
import java.util.*


class VisitActivity:AppCompatActivity() {

    val geocoder = Geocoder(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_visit)
        visit_mapview.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(36.1648803, 127.4809185), true);
        visit_mapview.setZoomLevel(11, true)
//        var address="서울특별시 노원구 월계2동 845-13"
        var address="서울특별시 용산구 서빙고로 137"
        var geo=geocoder.getFromLocationName(address, 1)
        var geo_lat=geo[0].latitude
        var geo_long=geo[0].longitude

        Log.w("MyCurrentloctionaddress", geo[0].toString())
        Log.i("visitgeocode", "$geo_lat, $geo_long")

        var marker=MapPOIItem()
        marker.itemName=address
        marker.mapPoint=MapPoint.mapPointWithGeoCoord(geo[0].latitude, geo[0].longitude)
        visit_mapview.addPOIItem(marker)

        visit_allbtn.setOnClickListener {
            var intent = Intent(this, VisitlistActivity::class.java)
            startActivity(intent)
        }



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

}