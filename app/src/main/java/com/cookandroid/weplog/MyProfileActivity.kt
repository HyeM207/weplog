package com.cookandroid.weplog

import android.graphics.Color
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.mypage_profile.*


class MyProfileActivity:AppCompatActivity() {

    //db용
    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage_profile)

        database = Firebase.database.reference
        database.child("users/$uid").get().addOnSuccessListener {
            var post = it.getValue<User>()

//            profile_nickname.text=post?.nickname.toString()
            profile_nickname.setText("${post?.nickname}")
            profile_phone.setText("${post?.phone}")
            profile_email.setText("${post?.email}")

            profile_phone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
            profile_email.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                        profile_emailalert.setText("잘못된 이메일 형식입니다.")
                    } else {
                        profile_emailalert.setText("")
                    }
                } // afterTextChanged()..
            })



            Log.i("firebase", "got profile value $post")


        }.addOnFailureListener {
            Log.e("firebase", "error getting data in profile")
        }

        my_profile_donebtn.setOnClickListener {
            var User = User()

            User.nickname = profile_nickname.text.toString()
            User.phone = profile_phone.text.toString()

            var UserValues = User.toMap()

            database.child("users/$uid").updateChildren(UserValues).addOnSuccessListener {
                finish()
            }

        }






    }

}