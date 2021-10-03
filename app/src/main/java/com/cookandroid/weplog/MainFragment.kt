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

    fun update(day : String, month : String, year : String){
        var date = year + "/" + month + "/" + day
        database = Firebase.database.reference
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

        // main 페이지 접근 시 로그인 되어 있는지 확인
        val user = Firebase.auth.currentUser
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


        // nickname & grade 이미지 설정
        val userRef = Firebase.database.getReference("users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(user?.uid.toString()).child("nickname").value
                main_nickname.setText(name.toString())

                val grade = snapshot.child(user?.uid.toString()).child("grade").value.toString()
                Log.e("grade", grade +" grade")
                when(grade){
                    "1"-> main_lv.setImageResource(R.drawable.yellow_circle)
                    "2"-> main_lv.setImageResource(R.drawable.green2_circle)
                    "3"-> main_lv.setImageResource(R.drawable.blue2_circle)
                    "4"-> main_lv.setImageResource(R.drawable.red_circle)
                    "5"-> main_lv.setImageResource(R.drawable.purple2_circle)
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




}



