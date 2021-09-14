package com.cookandroid.weplog

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase


class MainFragment : Fragment() {

    lateinit var main_nickname : TextView
    lateinit var main_logoutBtn : Button
    lateinit var mainRecordLayout : ConstraintLayout

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

        // main 페이지 접근 시 로그인 되어 있는지 확인
        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(activity, "[Main] user가 null", Toast.LENGTH_SHORT).show()
            var intent = Intent(activity, Login::class.java)
            startActivity(intent)

        }


        // nickname 설정
        val userRef = Firebase.database.getReference("users")

        userRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val name = snapshot.child(user?.uid.toString()).child("nickname").value
            main_nickname.setText(name.toString())
        }

        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Post failed, log a message
        }
        })



        // 로그아웃
        main_logoutBtn.setOnClickListener {
            Firebase.auth.signOut()
            var intent = Intent(activity, Login::class.java)
            startActivity(intent)
        }

        // record 창 누르면 record 페이지로 이동
        mainRecordLayout.setOnClickListener {
            var intent = Intent(activity, RecordActivity::class.java) //로그인 페이지 이동
            startActivity(intent)
        }

        return view

    }


}