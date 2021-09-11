package com.cookandroid.weplog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference

class Login : AppCompatActivity() {

    private lateinit var database: DatabaseReference







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


        database = Firebase.database.reference

    }
}