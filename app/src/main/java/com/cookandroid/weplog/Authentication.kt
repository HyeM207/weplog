package com.cookandroid.weplog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class Authentication : AppCompatActivity() {

    lateinit var auth_btnStep1Ok : Button
    lateinit var auth_btnStep1No : Button
    lateinit var auth_layoutStep2 : ConstraintLayout
    lateinit var auth_btnStep2 : Button
    lateinit var auth_layoutUpload : ConstraintLayout
    lateinit var auth_btnskip : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication)

        auth_btnStep1Ok = findViewById(R.id.auth_btnStep1Ok)
        auth_btnStep1No = findViewById(R.id.auth_btnStep1No)
        auth_layoutStep2 = findViewById(R.id.auth_layoutStep2)
        auth_btnStep2 = findViewById(R.id.auth_btnStep2)
        auth_layoutUpload = findViewById(R.id.auth_layoutUpload)
        auth_btnskip = findViewById(R.id.auth_btnskip)

        // 1단계. 인증하기 버튼
        auth_btnStep1Ok.setOnClickListener {
            auth_layoutStep2.visibility = View.VISIBLE
        }

        // 1단계. 괜찮습니다 버튼
        auth_btnStep1No.setOnClickListener {
            auth_layoutStep2.visibility = View.VISIBLE
        }

        // 2단계. 사진찍기 버튼
        auth_btnStep2.setOnClickListener {
            auth_layoutUpload.visibility = View.VISIBLE
        }

        // 최종 인증
        auth_layoutUpload.setOnClickListener{

        }

        // skip 버튼
        auth_btnskip.setOnClickListener {

        }


    }
}