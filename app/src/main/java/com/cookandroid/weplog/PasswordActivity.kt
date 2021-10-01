package com.cookandroid.weplog

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.mypage_password.*


class PasswordActivity:AppCompatActivity() {

    //db용
    private var auth : FirebaseAuth? = null
    private lateinit var database: DatabaseReference
    private val CurrentUser = FirebaseAuth.getInstance().currentUser
    val uid = CurrentUser?.uid
    lateinit var pass:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage_password)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference


        btn_pwcheck.setOnClickListener {
            database.child("users/$uid/password").get().addOnSuccessListener {
                pass=it.value.toString()
                if (pass==""){
                    //구글 로그인인 경우
                    Log.i("pw", "this is google login")
                    showAlert(1) //비밀번호 변경 불가 알림창
                }else{
                    //일반 로그인인 경우
                    Log.i("pw", "this is email login")
                    if(firstpw.text.toString() != ""){
                        //비밀번호 유효성 검사
                        if(pwCheck(firstpw.text.toString())){
                            //비밀번호 일치영부 확인
                            if(firstpw.text.toString().equals(secondpw.text.toString())) {
                                //기존 비밀번호와 일치 여부 확인
                                if(edit_withdrawal.text.toString().equals(pass)){
                                    database.child("users/$uid/password").setValue(secondpw.text.toString())
                                    showAlert(5)
                                }else{
                                    //기존 비밀번호가 일치하는 않는 경우
                                    showAlert(4)
                                }
                            }else{
                                //1차, 2차 비밀번호가 일치하지 않는 경우
                                showAlert(2)
                            }
                        }else{
                            //비밀번호 조건을 만족하지 않은 경우
                            showAlert(3)
                        }

                    }

                }



            }


        }


    }

    fun pwCheck(pw : String): Boolean{
        //pw 조건 : 숫자/문자/특수문자 2개이상 포함, 8~15자
        return pw.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9!@#\$%^&*]).{8,15}\$".toRegex())
    }

    fun showAlert(menu : Int){
        var msg=""
        if (menu==1){
            msg="SNS 로그인은 비밀번호 변경이 불가능합니다."
        }else if (menu==2){
            msg="두 비밀번호가 일치하지 않습니다."
        }else if (menu==3){
            msg="비밀번호는 숫자/문자/특수문자를 2개 이상 포함해야 하고 8~15자를 만족해야 합니다."
        }else if (menu==4){
            msg="잘못된 비밀번호를 입력했습니다."
        }else if (menu==5){
            msg="비밀번호가 변경되었습니다."
        }else{
            Log.e("showAlert", "잘못된 매개변수 입력")
        }

        AlertDialog.Builder(this)
            .setTitle("알림")
            .setMessage(msg)
            .setPositiveButton("ok", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    Log.d("MyTag", "positive")
                    if(menu==5){
//                        System.exit(0)
                        finish()
                        overridePendingTransition(0,0)
                    }
                }
            }).create().show()
    }





}