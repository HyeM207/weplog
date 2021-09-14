package com.cookandroid.weplog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var main_nickname : TextView
    lateinit var main_logoutBtn : Button

    private var auth : FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        auth = FirebaseAuth.getInstance()

        main_nickname = findViewById(R.id.main_nickname)
        main_logoutBtn = findViewById(R.id.main_logoutBtn)


        // main 페이지 접근 시 로그인 되어 있는지 확인
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "[Main] user가 null", Toast.LENGTH_SHORT).show()
            var intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        Toast.makeText(this, "[Main] main으로 옴", Toast.LENGTH_SHORT).show()

        // nickname 설정
        main_nickname.setText(user?.uid.toString())


        // 로그아웃
        main_logoutBtn.setOnClickListener {
            auth!!.signOut()

            var intent=Intent(this,Login::class.java) //로그인 페이지 이동
            startActivity(intent)
            finish()
        }

    }
}