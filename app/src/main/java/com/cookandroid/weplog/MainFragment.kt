package com.cookandroid.weplog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dinuscxj.progressbar.CircleProgressBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main.*
import kotlinx.android.synthetic.main.main_history.*
import kotlinx.android.synthetic.main.map.*
import java.util.*


class MainFragment : Fragment() {

    lateinit var main_nickname : TextView
    lateinit var main_kcaltxt : TextView
    lateinit var main_steptxt : TextView
    lateinit var main_timetxt : TextView
    lateinit var main_logoutBtn : Button
    lateinit var mainRecordLayout : ConstraintLayout
    lateinit var mainPlogLayout:ConstraintLayout
    lateinit var mainAreaLayout:ConstraintLayout
    lateinit var main_lv : ImageView
    private var mCircleProgressBar : CircleProgressBar?= null // 원형 그래프 (오늘의 스텝 수)
    private lateinit var database: DatabaseReference
    private var auth : FirebaseAuth? = null
    lateinit var main_todayAuth : TextView

    val user = Firebase.auth.currentUser
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid
    var leftcredit = 0
    var lvname= arrayOf("Yellow", "Green", "Blue", "Red", "Purple")

    var visitlist = ArrayList<VisitArea>()
    var bigareaList= ArrayList<String>()
    var trashareaList= ArrayList<String>()
    var middleareaList= ArrayList<String>()
    private var titleList: List<String>? = null
    private var countList= ArrayList<Int>()


    var listData = HashMap<String, List<String>>()
    var childList = ArrayList<String>()
    var headerList = ArrayList<String>()

    var visitcountsum=0

    //기록 계산 변수
    var distSum=0F
    var timeSum=0
    var plogSum=0
    var historyList=ArrayList<History>()



    fun update(day : String, month : String, year : String){
        var date = year + "/" + month + "/" + day
        database = Firebase.database.reference


        checkLv()
        loadvisit()
        setPlog()

        database.child("user").child(Firebase.auth.currentUser!!.uid).child("Pedometer").child("date").child(date).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var c = 0
                var dis = 0f
                var time : String ?= null
                var w = 0
                var j = 0
                var r = 0
                var kcal = 0f
                var hour = 0
                var min = 0
                var sec = 0
                for ( snap in snapshot.children ){
                    c = c + snap.child("step").childrenCount.toInt()
                    var v = snap.child("step").child("type").value
                    if ( v.toString() == "0" ){
                        w = w + 1
                    } else if ( v.toString() == "1"){
                        j = j + 1
                    } else {
                        r = r + 1
                    }
                    var bo = false
                    for ( s in snap.children ){
                        if ( s.key.toString() == "record" ) {
                            bo = true
                        }
                    }
                    if (bo == false){
                        dis = dis + 0f
                    } else {
                        dis = dis + snap.child("record").child("distance").value.toString().toFloat()
                        time = snap.child("record").child("time").value.toString()
                        var split_time = time!!.split(":")
                        hour = hour + split_time[0].toInt()
                        min = min + split_time[1].toInt()
                        sec = sec + split_time[2].toInt()
                    }

                }
                // CircleGraph
                mCircleProgressBar!!.max = 500
                mCircleProgressBar!!.progress = c
                mCircleProgressBar!!.setProgressFormatter(CircleProgressBar.ProgressFormatter { progress, max ->
                    val pattern = "%d Steps"
                    String.format(pattern, progress)
                })

                kcal = w*0.05f + j*0.1f + r*0.2f
                main_kcaltxt!!.text = kcal.toString() + "Kcal"
                main_steptxt!!.text = dis.toString()

                println(hour.toString() + ":" + min.toString() + ":" + sec.toString())
                main_timetxt!!.text = String.format("%02d:%02d:%02d", (min + sec / 60) / 60 + hour, (min + sec / 60) % 60, (sec % 60))

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.main, container, false)

        auth = FirebaseAuth.getInstance()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()


        main_nickname = view.findViewById(R.id.main_nickname)
        main_logoutBtn = view.findViewById(R.id.main_logoutBtn)
        mainRecordLayout = view.findViewById(R.id.mainRecordLayout)
        mainPlogLayout = view.findViewById(R.id.mainPlogLayout)
        mainAreaLayout = view.findViewById(R.id.mainAreaLayout)
        main_lv = view.findViewById(R.id.main_lv)
        mCircleProgressBar = view.findViewById(R.id.main_step)
        main_steptxt = view.findViewById(R.id.main_steptxt)
        main_kcaltxt = view.findViewById(R.id.main_kcaltxt)
        main_timetxt = view.findViewById(R.id.main_timetxt)
        main_todayAuth = view.findViewById(R.id.main_todayAuth)

        // main 페이지 접근 시 로그인 되어 있는지 확인
        if (user == null) {
//            Toast.makeText(activity, "[Main] user가 null", Toast.LENGTH_SHORT).show()
            var intent = Intent(activity, Login::class.java)
            startActivity(intent)

        }

        var mCalendar = Calendar.getInstance()
        var main_month = (mCalendar.get(Calendar.MONTH) + 1).toString()
        var main_year = (mCalendar.get(Calendar.YEAR)).toString()
        var main_day = (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()

        // step 그래프
        update(main_day.toString(), main_month.toString(), main_year.toString())


        // nickname & grade 이미지 설정 & 오늘의 인증 설정
        val userRef = Firebase.database.getReference("users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 닉네임 설정
                val name = snapshot.child(user?.uid.toString()).child("nickname").value
                main_nickname.setText(name.toString())


                // grade 설정
                val grade = snapshot.child(user?.uid.toString()).child("grade").value.toString()
                Log.e("grade", grade +" grade")
                when(grade){
                    "1"-> main_lv.setImageResource(R.drawable.yellow_circle)
                    "2"-> main_lv.setImageResource(R.drawable.green2_circle)
                    "3"-> main_lv.setImageResource(R.drawable.blue2_circle)
                    "4"-> main_lv.setImageResource(R.drawable.red_circle)
                    "5"-> main_lv.setImageResource(R.drawable.purple2_circle)
                }

                // 오늘의 인증 확인
                val lastAuth = snapshot.child("lastAuth").value

                if (lastAuth == null){  // 마지막 플러깅 기록이 null 일 때
                    main_todayAuth.text = "플러깅을 하고 인증하세요."
                }
                else{
                    var mCalendar = Calendar.getInstance()
                    var todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()

                    if (! lastAuth.toString().equals(todayDate)){ // 오늘 인증 한 것이 없을때
                        main_todayAuth.text = "플러깅을 하고 인증하세요."
                    }
                    else{
                        var lastAuthPost = snapshot.child("lastAuthPost").value
                        if (lastAuthPost != null)
                        {

                            var postRef = Firebase.database.getReference("community").child(lastAuthPost.toString())
                            postRef.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        var certified = snapshot.child("certified").value.toString()
                                        var authCount = snapshot.child("authCount").value.toString()
                                        Log.e("main", certified +", "+authCount)
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }
                                    })
                                            ///
                        }
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        })







        // 내가 쓴글 보기
        main_nickname.setOnClickListener {
            var intent = Intent(activity, MyPost::class.java)
            startActivity(intent)
        }



        // 로그아웃
        main_logoutBtn.setOnClickListener {

            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("804853471593-vruv3i2eeu2t0n3je5i2np56uh5oevgg.apps.googleusercontent.com")
                .requestEmail()
                .build()



//            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                    .requestIdToken(getString(R.string.default_web_client_id))
//                    .requestEmail()
//                    .build()


            var googleSignInClient : GoogleSignInClient? = null
            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)



            Firebase.auth.signOut()
            FirebaseAuth.getInstance().signOut()
            googleSignInClient?.signOut()  // 구글 로그인 세션까지 로그아웃 처리

            var intent = Intent(activity, Login::class.java)
            startActivity(intent)

        }

        // record 창 누르면 record 페이지로 이동
        mainRecordLayout.setOnClickListener {
            var intent = Intent(activity, RecordActivity::class.java) //로그인 페이지 이동
            startActivity(intent)
        }

        mainPlogLayout.setOnClickListener {
            var intent = Intent(activity, HistoryActivity::class.java) //로그인 페이지 이동
            startActivity(intent)
        }

        mainAreaLayout.setOnClickListener {
            var intent = Intent(activity, VisitActivity::class.java) //로그인 페이지 이동
            startActivity(intent)
        }

        return view

    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

    }

    fun checkLv(){

        // nickname & grade 이미지 설정
        val userRef = Firebase.database.getReference("users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                val grade = snapshot.child(user?.uid.toString()).child("grade").value.toString()

                Log.e("grade", grade +" grade")
                var upcredit=0

                when(grade){
                    "1"-> upcredit=20
                    "2"-> upcredit=50
                    "3"-> upcredit=100
                    "4"-> upcredit=200
                    "5"-> upcredit=300
                }
                // credit 불러오기
                val credit = snapshot.child(user?.uid.toString()).child("credit").value.toString().toInt()

                if (upcredit!=0){
                    if (upcredit==300){
                        main_lvsectxt.text=String.format("현재 최고 등급입니다.")
                    }else{
                        leftcredit=upcredit-credit
                        main_lvsectxt.text=String.format("다음 등급까지 남은 크레딧 : %d", leftcredit)
                    }
                }


                main_leveltext.text=String.format("현재 %s 등급입니다.", lvname.get(grade.toInt()-1))


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        })

    }


    fun loadvisit() {


        database.child("user/$uid/visit").get().addOnSuccessListener {
            var post = it.children

            for (p in post) {
                var pbig = p.key
                Log.i("firebase", "bigarea $pbig")
                bigareaList.add("$pbig")
            }

            //년도
            for (y in bigareaList) {
                post = it.child("$y").children
                middleareaList.clear()

                //큰 지역 아래 구역 가져오기 (시/군/구)
                for (p in post) {
                    var pmid = p.key
                    middleareaList.add("$pmid")
                    Log.i("firebase", "middle area $pmid")
                }

                for (mid in middleareaList) {

                    trashareaList.clear()
                    //해당 월에서 일 가져오기
                    var dayPost = it.child("$y/$mid").children
                    Log.i("firebase", "check mid $mid")

                    for (d in dayPost) {
                        var pday = d.key
                        Log.i("firebase", "trasharea $pday")
                        trashareaList.add("$pday")
                    }


                    for (day in trashareaList) {
                        var dayCount = it.child("$y/$mid/$day/count").value
                        Log.i("firebase", "daydata check $dayCount, day : $day")

                        var visitarea = VisitArea()
                        visitarea.bigarea = y
                        visitarea.middlearea = mid
                        visitarea.trasharea = day
                        visitarea.count = it.child("$y/$mid/$day/count").value.toString().toInt()

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
                countList.add(listData[title]!!.size)
            }

            for (c in countList){
                visitcountsum+=c
            }

            mainAreaText.text=String.format("%d개", visitcountsum)






        }
    }



    fun setPlog(){
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
                    historyList.add(History)
                }

                mainNumText.text="${plogSum}회"


            }



        }

    }




}



