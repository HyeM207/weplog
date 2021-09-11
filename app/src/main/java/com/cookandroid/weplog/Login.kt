package com.cookandroid.weplog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button



class Login : AppCompatActivity() {

    //private lateinit var database: DatabaseReference

    lateinit var login_btn2 : Button






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //database = Firebase.database.reference

        login_btn2 = findViewById(R.id.login_btn2)


        login_btn2.setOnClickListener {
            var intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }



    }
}