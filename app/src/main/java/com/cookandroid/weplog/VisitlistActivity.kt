package com.cookandroid.weplog

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.Toast
import com.cookandroid.weplog.ExpandableListData.data
import java.util.HashMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class VisitlistActivity:AppCompatActivity() {

    //db용
    private lateinit var database: DatabaseReference
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid

    private var expandableListView: ExpandableListView? = null
    private var adapter: ExpandableListAdapter? = null
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
//        setContentView(R.layout.visit_list)
        setContentView(R.layout.expandable_list)

        expandableListView = findViewById(R.id.expendableList)
        database = Firebase.database.reference
        setData()



    }

    fun setData(){
        var expandableListDetail = HashMap<String, List<String>>()
        var trashlist: MutableList<String> = ArrayList<String>()

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
                    //해당 월에서 일 가져오기
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
//                        listData.put(h, childList)
                    }
                }

                if(childList.isNotEmpty()){
                    listData.put(h, childList)
                }

            }




            if (expandableListView != null) {

                titleList = ArrayList(listData.keys)
                countList.clear()

                for (title in titleList as ArrayList<String>){
                    countList.add(listData[title]!!.size)
                }

                Log.i("firebase", "check title list $titleList")
                adapter = CustomExpandableListAdapter(this, titleList as ArrayList<String>, countList, listData)
                expandableListView!!.setAdapter(adapter)
                expandableListView!!.setOnGroupExpandListener { groupPosition ->
//                Toast.makeText(
//                    applicationContext,
//                    (titleList as ArrayList<String>)[groupPosition] + " List Expanded.",
//                    Toast.LENGTH_SHORT
//                ).show()
                }
                expandableListView!!.setOnGroupCollapseListener { groupPosition ->
//                Toast.makeText(
//                    applicationContext,
//                    (titleList as ArrayList<String>)[groupPosition] + " List Collapsed.",
//                    Toast.LENGTH_SHORT
//                ).show()
                }
                expandableListView!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                    Toast.makeText(
                        applicationContext,
                        "Clicked: " + (titleList as ArrayList<String>)[groupPosition] + " -> " + listData[(
                                titleList as
                                        ArrayList<String>
                                )
                                [groupPosition]]!!.get(
                            childPosition
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                }
            }





        }



    }


}