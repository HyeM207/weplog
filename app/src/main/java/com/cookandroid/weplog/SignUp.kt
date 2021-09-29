package com.cookandroid.weplog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class SignUp : AppCompatActivity() {


    lateinit var signup_email : EditText
    lateinit var signup_nickname : EditText
    lateinit var signup_password : EditText
    lateinit var signup_phone : EditText
    lateinit var signup_signBtn : Button

    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        setTitle("회원가입")

        database = Firebase.database.reference
        auth = FirebaseAuth.getInstance()


        signup_email = findViewById(R.id.signup_email)
        signup_password = findViewById(R.id.signup_password)
        signup_nickname = findViewById(R.id.signup_nickname)
        signup_phone = findViewById(R.id.signup_phone)
        signup_signBtn = findViewById(R.id.signup_signBtn)


        // 회원가입 절차
        signup_signBtn.setOnClickListener {
            Toast.makeText(this, "버튼 눌림", Toast.LENGTH_LONG).show()
            var email = signup_email.text.toString()
            var password = signup_password.text.toString()

            if (email.length < 1 || password.length < 1) {
                Toast.makeText(this, "입력칸이 공란입니다.", Toast.LENGTH_SHORT).show()
            } else {
                auth?.createUserWithEmailAndPassword(email, password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                // Firebase Realtime database에 User 정보 저장
                                val CurrentUser = FirebaseAuth.getInstance().currentUser
                                var User = User()

                                User.uid = CurrentUser?.uid
                                User.email = CurrentUser?.email
                                User.password = signup_password.text.toString()  // 우선은 암호화하지 않고 저장함
                                User.nickname = signup_nickname.text.toString()
                                User.phone = signup_phone.text.toString()

                                var mCalendar = Calendar.getInstance()
                                var todayDate = (mCalendar.get(Calendar.YEAR)).toString() + "/" + (mCalendar.get(Calendar.MONTH) + 1).toString() + "/" + (mCalendar.get(Calendar.DAY_OF_MONTH)).toString()
                                User.joindate = todayDate


                                var UserValues = User.toMap()


                                val uid = CurrentUser?.uid

                                if (uid != null) {
                                    database.child("users").child(uid).setValue(UserValues)
                                }
                                /*
                                val UserUpdates = hashMapOf<String,Any>(
                                    "/User/$uid" to UserValues
                                )
                                  database.updateChildren(UserUpdates)
                                */

                                Toast.makeText(this, "회원가입 완료", Toast.LENGTH_LONG).show()


                                if (auth!!.currentUser != null) {
                                    var intent = Intent(this, NavigationActivity::class.java)
                                    startActivity(intent)
                                }

                            } else {
                                //show the error message
                                Toast.makeText(
                                        this,
                                        task.exception?.message + "회원가입 실패",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
            }
        }





        //val myRef : DatabaseReference = database.getReference("message")
        //myRef.setValue("안녕 반가워!")

    }







}