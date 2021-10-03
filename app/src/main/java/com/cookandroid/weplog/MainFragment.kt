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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.main.*
import retrofit2.http.GET


class MainFragment : Fragment() {

    lateinit var main_nickname : TextView
    lateinit var main_logoutBtn : Button
    lateinit var mainRecordLayout : ConstraintLayout
    lateinit var mainPlogLayout:ConstraintLayout
    lateinit var mainAreaLayout:ConstraintLayout
    lateinit var main_lv : ImageView

    private var auth : FirebaseAuth? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.main, container, false)

        auth = FirebaseAuth.getInstance()


        main_nickname = view.findViewById(R.id.main_nickname)
        main_logoutBtn = view.findViewById(R.id.main_logoutBtn)
        mainRecordLayout = view.findViewById(R.id.mainRecordLayout)
        mainPlogLayout = view.findViewById(R.id.mainPlogLayout)
        mainAreaLayout = view.findViewById(R.id.mainAreaLayout)
        main_lv = view.findViewById(R.id.main_lv)

        // main 페이지 접근 시 로그인 되어 있는지 확인
        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(activity, "[Main] user가 null", Toast.LENGTH_SHORT).show()
            var intent = Intent(activity, Login::class.java)
            startActivity(intent)

        }


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
/*
            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()


            var googleSignInClient : GoogleSignInClient? = null
            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)



            Firebase.auth.signOut()
            FirebaseAuth.getInstance().signOut()
            googleSignInClient?.signOut()  // 구글 로그인 세션까지 로그아웃 처리
*/
            var intent = Intent(activity, Authentication::class.java)
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




}



