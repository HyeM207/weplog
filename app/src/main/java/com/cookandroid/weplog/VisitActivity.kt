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
import kotlin.collections.ArrayList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class VisitActivity:AppCompatActivity() {

    //db용
    private lateinit var database: DatabaseReference
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid

    val geocoder = Geocoder(this)
    var addressList = ArrayList<String>()

    private var titleList: List<String>? = null
    private var countList= ArrayList<Int>()

    var listData = HashMap<String, List<String>>()
    var childList = ArrayList<String>()
    var headerList = ArrayList<String>()
    var visitlist = ArrayList<VisitArea>()
    var bigareaList= ArrayList<String>()
    var trashareaList= ArrayList<String>()
    var middleareaList= ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_visit)
        visit_mapview.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(36.1648803, 127.4809185), true);
        visit_mapview.setZoomLevel(11, true)
        visit_mapview.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        visit_mapview.setShowCurrentLocationMarker(false)

        database = Firebase.database.reference

        setAddress()

        visit_allbtn.setOnClickListener {
            var intent = Intent(this, VisitlistActivity::class.java)
            startActivity(intent)

        }



    }

    fun setAddress(){

        database.child("user/$uid/visit").get().addOnSuccessListener {

            var post = it.children

            for(p in post) {
                var pbig = p.key
                Log.i("firebase", "bigarea $pbig")
                bigareaList.add("$pbig")
            }
//            bigareaList.reverse()

            //년도
            for (y in bigareaList){
                post=it.child("$y").children
                middleareaList.clear()

                //큰 지역 아래 구역 가져오기 (시/군/구)
                for (p in post){
                    var pmid = p.key
                    middleareaList.add("$pmid")
                    Log.i("firebase", "middle area $pmid")
                }
//                monthList.reverse()


                for (mid in middleareaList){

                    trashareaList.clear()
                    //쓰레기통 위치 저장
                    var dayPost = it.child("$y/$mid").children
                    Log.i("firebase", "check mid $mid")

                    for (d in dayPost){
                        var pday = d.key
                        Log.i("firebase", "trasharea $pday")
                        trashareaList.add("$pday")
                    }


                    for (day in trashareaList){
                        var dayCount=it.child("$y/$mid/$day/count").value
                        Log.i("firebase", "daydata check $dayCount, day : $day")

                        var visitarea = VisitArea()
                        visitarea.bigarea=y
                        visitarea.middlearea=mid
                        visitarea.trasharea=day
                        visitarea.count=it.child("$y/$mid/$day/count").value.toString().toInt()
//                        visitarea.count=0

                        visitlist.add(visitarea)
                    }


                }

            }

            for (v in visitlist){
//                childList.add(String.format("%s %s %s", v.bigarea, v.middlearea, v.trasharea))
//                childList.add(v.trasharea)
                headerList.add(String.format("%s %s", v.bigarea, v.middlearea))

            }

            //headerlist 중복데이터 제거
            var header_distinct=headerList.distinct()
            for (h in header_distinct){
                childList= ArrayList<String>()
                for (v in visitlist){
                    var check_header=String.format("%s %s", v.bigarea, v.middlearea)
                    Log.i("firebase", "check header list $check_header, header distinct : $h")

                    if(check_header == h){
                        childList.add(v.trasharea)
                    }
                }

                if(childList.isNotEmpty()){
                    listData.put(h, childList)
                }

            }

            titleList = ArrayList(listData.keys)
            countList.clear()

            for (title in titleList as ArrayList<String>){

                var geo=geocoder.getFromLocationName(title, 1)


                var marker=MapPOIItem()
                marker.itemName=String.format("%s : %d", title, listData[title]!!.size)
                marker.mapPoint=MapPoint.mapPointWithGeoCoord(geo[0].latitude, geo[0].longitude)
                visit_mapview.addPOIItem(marker)




            }



//            for (v in visitlist){
////                childList.add(String.format("%s %s %s", v.bigarea, v.middlearea, v.trasharea))
////                childList.add(v.trasharea)
//
//                var address=String.format("%s %s", v.bigarea, v.middlearea)
//                var geo=geocoder.getFromLocationName(address, 1)
//                var geo_lat=geo[0].latitude
//                var geo_long=geo[0].longitude
//
//                var marker=MapPOIItem()
////                marker.itemName=address
//                marker.itemName=String.format("%s : %d", address, v.count)
//                marker.mapPoint=MapPoint.mapPointWithGeoCoord(geo[0].latitude, geo[0].longitude)
//                visit_mapview.addPOIItem(marker)
//
//            }



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