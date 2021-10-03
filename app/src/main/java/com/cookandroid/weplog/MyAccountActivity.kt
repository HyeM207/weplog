package com.cookandroid.weplog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.model.stream.MediaStoreImageThumbLoader
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.mypage_account.*
import java.lang.Exception

class MyAccountActivity:AppCompatActivity() {
    lateinit var builder : AlertDialog.Builder
    lateinit var mAuth :FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage_account)

        val items= mutableListOf<ListViewItem>()

        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_article_24), "개인정보 수정"))
        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_lock_24), "비밀번호 변경"))
        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_power_settings_new_24), "로그아웃"))
        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_block_24), "회원탈퇴"))

        // main 페이지 접근 시 로그인 되어 있는지 확인
        val user = Firebase.auth.currentUser
        if (user == null) {
            Toast.makeText(this, "[Main] user가 null", Toast.LENGTH_SHORT).show()
            var intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        mAuth = FirebaseAuth.getInstance();


        val adapter=ListViewAdapter(items)
        my_accountlist.adapter=adapter

        my_accountlist.setOnItemClickListener{ parent : AdapterView<*>, view: View, position:Int, id:Long ->
            val item=parent.getItemAtPosition(position) as ListViewItem
            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()

            if (item.title=="개인정보 수정"){
                val profileIntent= Intent(this, MyProfileActivity::class.java)
                startActivity(profileIntent)
            }else if (item.title=="비밀번호 변경"){
                val pwIntent= Intent(this, PasswordActivity::class.java)
                startActivity(pwIntent)
            }else if (item.title=="로그아웃"){
                Firebase.auth.signOut()
                var intent = Intent(this, Login::class.java)
                startActivity(intent)
            } else if (item.title=="회원탈퇴"){
                withdrawal()
            }

        }


    }

    private fun withdrawal() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.withdrawal_edittext, null)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("회원탈퇴")
            .setMessage("탈퇴 시 복구 불가능합니다. 탈퇴를 원하시면 \"탈퇴\"를 입력해주세요")
            .setPositiveButton("확인") { dialog, which ->
                val withdrawal_edittext: EditText = view.findViewById(R.id.withdrawal_edittext)
                var check_str = withdrawal_edittext.text.toString().trim()
                if(check_str.equals("탈퇴")) {

                    val user = Firebase.auth.currentUser
                    var database = Firebase.database.reference

                    Log.e("delete", "here")


                        try {
                            database.child("users").child(user?.uid.toString()).get().addOnSuccessListener {

                                Log.e("delete", "pos")

                                if(it.child("posts").exists()) {

                                val postList: Map<String, Object> ?= it.child("posts").value as Map<String, Object>

                                if (postList != null) {
                                    Log.e("delete", "postList : " + postList.toString())

                                    for (post in postList) {
                                        Log.e("delete", "post${post.key}")

                                        // 1. 연결된 사진 삭제
                                        database.child("community").child(post.key).get()
                                                .addOnSuccessListener {
                                                    var photoUrl = it.child("photoUrl").value.toString()
                                                    Log.e("delete", "it :$photoUrl")

                                                    Firebase.storage.reference.child("community/${photoUrl}")
                                                            .delete().addOnSuccessListener {
                                                                Log.e("delete", "삭제 성공!")
                                                            }.addOnFailureListener {
                                                                Log.e("delete", "삭제 실패!" + it)
                                                            }
                                                }

                                        // 2. community의 post 객체 삭제
                                        var postRef = Firebase.database.getReference("community").child(post.key)
                                        //                            Log.e("post",postRef.toString())
                                        postRef.setValue(null)
                                        Log.e("post", post.key)
                                    }
                                }
                            }
                            }

/*
                            var userRef = Firebase.database.getReference("users").child(Firebase.auth.currentUser?.uid.toString())
                            userRef.setValue(null)



                            // (일반) 5. auth에서 삭제
                            val user = Firebase.auth.currentUser!!
                            user.delete().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.e("delete", "User account deleted.")
                                    }
                            }.addOnFailureListener {
                                Log.e("delete", "실패!@!@!.")
                            }



                            // (일반)로그아웃 및 auth에서 getCurrentUser 삭제
                            Firebase.auth.signOut()
                            mAuth.getCurrentUser()?.delete();    // (구글) 회원 탈퇴 - auth 삭제
*/

                            // (일반) 4. user 객체 삭제
                            var userRef = Firebase.database.getReference("users").child(Firebase.auth.currentUser?.uid.toString())
                            //userRef.setValue(null)
                            userRef.removeValue().addOnSuccessListener { // user 객체 삭제
/*
                                // 구글 로그인 로그아웃
                                var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(getString(R.string.default_web_client_id))
                                        .requestEmail()
                                        .build()

                                var googleSignInClient : GoogleSignInClient? = null
                                googleSignInClient = GoogleSignIn.getClient(this, gso)

                                googleSignInClient?.signOut()  // 구글 로그인 세션까지 로그아웃 처리
                                Firebase.auth.signOut()
*/
                                /*
                                FirebaseAuth.getInstance().currentUser!!.delete().addOnSuccessListener { // user 객체 삭제 성공하면 auth이 객체 삭제
                                    Toast.makeText(this, "탈퇴 성공", Toast.LENGTH_SHORT).show()
                                    var intent = Intent(this, Login::class.java) //로그인 페이지 이동
                                    startActivity(intent)
                                }

                                 */

                                Firebase.auth.currentUser!!.delete().addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.e("delete", "User account deleted.")
                                        var intent = Intent(this, Login::class.java) //로그인 페이지 이동
                                        startActivity(intent)
                                    }
                                }.addOnFailureListener {
                                    Log.e("delete", "실패!@!@!.")
                                }


                            }


                        } catch (e : Exception) {
                            Log.e("delete", "$e + error")
                        }


                    }else{
                        Toast.makeText(this, "입력이 올바르지 않습니다. 정확한 문자열을 입력해주세요.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            .setNeutralButton("취소", null)
            .create()

        //  여백 눌러도 창 안없어지게
        alertDialog.setCancelable(false)

        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun logout() {
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        var googleSignInClient : GoogleSignInClient? = null
        googleSignInClient = GoogleSignIn.getClient(this, gso)



        Firebase.auth.signOut()
        FirebaseAuth.getInstance().signOut()
        googleSignInClient?.signOut()  // 구글 로그인 세션까지 로그아웃 처리
    }

}