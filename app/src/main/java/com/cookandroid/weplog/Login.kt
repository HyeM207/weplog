package com.cookandroid.weplog

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.login.*


class Login : AppCompatActivity() {


    lateinit var login_signUpBtn: Button
    lateinit var login_btn: Button
    lateinit var login_email: EditText
    lateinit var login_pw: EditText

    //private lateinit var login_googleBtn : GoogleSignInClient

    private var auth: FirebaseAuth? = null

    // 구글 로그인
    var googleSignInClient: GoogleSignInClient? = null
    val RC_SIGN_IN = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        login_signUpBtn = findViewById(R.id.login_signUpBtn)
        login_btn = findViewById(R.id.login_btn)
        login_email = findViewById(R.id.login_email)
        login_pw = findViewById(R.id.login_pw)


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Toast.makeText(this, "[Login] currentUser가 null이 아님", Toast.LENGTH_SHORT).show()
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        login_btn.setOnClickListener {
            login()
        }


        // /* 구글 로그인 */

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        //login_googleBtn = findViewById(R.id.login_googleBtn)
        login_googleBtn.setOnClickListener {
            signIn()
            Toast.makeText(this, "[Login] Google 버튼 누름", Toast.LENGTH_SHORT).show()
        }


        // /* 회원 가입 */
        login_signUpBtn.setOnClickListener {
            var intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            //val myRef : DatabaseReference = database.getReference("message")
            //myRef.setValue("안녕 반가워!")
        }

    }



    override fun onStart() {
        super.onStart()
            /*
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account!==null){ // 이미 로그인 되어있을시 바로 메인 액티비티로 이동
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

             */
        val currentUser = auth?.currentUser
        if (currentUser != null){
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


    }


    private fun login() {
        var email = login_email.text.toString()
        var password = login_pw.text.toString()

        if (email.length < 1 || password.length < 1) {
            Toast.makeText(this, "입력칸이 공란입니다.", Toast.LENGTH_SHORT).show()
        } else {
            auth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Login
                            //moveMainPage(task.result?.user)
                            Toast.makeText(this, "로그인 완료", Toast.LENGTH_SHORT).show()
                            if (auth!!.currentUser != null) {
                                Toast.makeText(this, "로그인 찐 완료", Toast.LENGTH_SHORT).show()
                                var intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            //show the error message
                            Log.w("Login", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                    this,
                                    task.exception?.message + "로그인 실패",
                                    Toast.LENGTH_LONG
                            ).show()

                        }
                    }
        }
    }


    // 구글 로그인
    private fun signIn() {
        Toast.makeText(this, "[Login] signIn 함수 실행", Toast.LENGTH_SHORT).show()
        val signInIntent : Intent = googleSignInClient!!.signInIntent
        startForResult.launch(signInIntent)
    }


    private val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

                if (result.resultCode == RESULT_OK) {
                    val intent: Intent = result.data!!
                    val task: Task<GoogleSignInAccount> =
                            GoogleSignIn.getSignedInAccountFromIntent(intent)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        if (account != null) {
                            Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                            Toast.makeText(this, "[Login] firebaseAuthWithGoogle", Toast.LENGTH_SHORT).show()
                            firebaseAuthWithGoogle(account.idToken!!)
                        }
                    }catch (e : ApiException){
                        Log.w(ContentValues.TAG, "Google sign in failed", e)
                        Toast.makeText(this, "[Login] Google sign in failed", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this, "[Login] startForResult 안 됨", Toast.LENGTH_SHORT).show()
                }
            }



     private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login", "signInWithCredential:success")
                        Toast.makeText(this, "[Login] signInWithCredential:success", Toast.LENGTH_SHORT).show()
                        val user = auth!!.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login", "signInWithCredential:failure", task.exception)
                        Toast.makeText(this, "[Login] signInWithCredential:failure", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
    }

    private fun updateUI(user: FirebaseUser?) { //update ui code here
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            //finish()
        }
    }
}
