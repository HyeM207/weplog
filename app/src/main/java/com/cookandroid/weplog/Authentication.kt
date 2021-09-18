package com.cookandroid.weplog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class Authentication : AppCompatActivity() {

    lateinit var auth_layoutStep1 : ConstraintLayout
    lateinit var auth_layoutStep2 : ConstraintLayout

    lateinit var auth_layoutUpload : ConstraintLayout
    lateinit var auth_btnskip : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.authentication)

        auth_layoutStep1 = findViewById(R.id.auth_layoutStep1)
        auth_layoutStep2 = findViewById(R.id.auth_layoutStep2)
        auth_layoutUpload = findViewById(R.id.auth_layoutUpload)
        auth_btnskip = findViewById(R.id.auth_btnskip)

        auth_layoutUpload.visibility = View.INVISIBLE

        // 1단계. 인증하기 버튼
       auth_layoutStep1.setOnClickListener{
           val intent = Intent(this, QRcodeScanner::class.java)
           startActivity(intent)
       }

        // 2단계. 사진찍기 버튼
        auth_layoutStep2.setOnClickListener{
            auth_layoutUpload.visibility = View.VISIBLE
            takePhoto()
        }

        // 최종 인증
        auth_layoutUpload.setOnClickListener{

        }

        // skip 버튼
        auth_btnskip.setOnClickListener {

        }


    }

    private fun takePhoto() {
        //String state
    }
}