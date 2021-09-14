package com.cookandroid.weplog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        setTitle("회원가입")
    }
}