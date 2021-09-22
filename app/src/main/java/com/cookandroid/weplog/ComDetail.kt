package com.cookandroid.weplog

import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text

class ComDetail : AppCompatActivity() {
    lateinit var comdetail_nick : TextView
    lateinit var comdetail_photo : ImageView
    lateinit var comdetail_text1 : TextView
    lateinit var comdetail_accept : Button
    lateinit var comdetail_deny : Button
    lateinit var comdetail_heart : ImageView
    lateinit var comdetail_check : ImageView
    lateinit var comdetial_certified : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comdetail)

        comdetail_nick = findViewById(R.id.comdetail_nick)
        comdetail_photo = findViewById(R.id.comdetail_photo)
        comdetail_text1 = findViewById(R.id.comdetail_text1)
        comdetail_accept = findViewById(R.id.comdetail_accept)
        comdetail_deny = findViewById(R.id.comdetail_deny)
        comdetail_heart = findViewById(R.id.comdetail_heart)
        comdetial_certified = findViewById(R.id.comdetial_certified)
        comdetail_check = findViewById(R.id.comdetail_check)


        // postId 가져옴
        val intent = intent
        val com_postId = intent.getStringExtra("com_postId")

        val user = Firebase.auth.currentUser



        var uid = FirebaseAuth.getInstance().currentUser?.uid

        // DB에서 불러오기
        val postRef = Firebase.database.getReference("community")

        if (com_postId != null) {
            postRef.child(com_postId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val getData = snapshot.getValue(Post::class.java)

                        //Toast.makeText(this, getData.postId)
                        comdetail_nick.text = getData?.writerNick.toString()

                        // 사진 불러오기
                        getData?.photoUrl?.let {
                            Firebase.storage.reference.child("community").child(it).downloadUrl.addOnCompleteListener {
                                if(it.isSuccessful){
                                    Glide.with(this@ComDetail)
                                            .load(it.result)
                                            .into(comdetail_photo)
                                }
                            }
                        }

                        // 자신의 글은 승인 못함
                        if (user?.uid.toString().equals(getData?.writerId.toString())){
                            comdetail_text1.visibility = View.GONE
                            comdetail_accept.visibility = View.GONE
                            comdetail_deny.visibility = View.GONE
                        }

                        // 인증 받은 글이면 visible 표기
                        if(getData?.certified == true){
                            comdetial_certified.visibility = View.VISIBLE
                        }

                        // 하트 표기
                        if (getData?.hearts?.containsKey(uid) == true){
                            comdetail_heart.setImageResource(R.drawable.heart)
                        }else{
                            comdetail_heart.setImageResource(R.drawable.noheart)
                        }

                        // 승인/거절한 게시물은 표시 x and 이미지 변경
                        if (getData?.auths?.containsKey(uid) == true) {
                            comdetail_text1.visibility = View.GONE
                            comdetail_accept.visibility = View.GONE
                            comdetail_deny.visibility = View.GONE
                            if (getData?.auths?.get(uid) == true){
                                comdetail_check.setImageResource(R.drawable.checkbox)
                            }else{
                                comdetail_check.setImageResource(R.drawable.cross_checkbox)
                            }
                        }else{
                            comdetail_check.setImageResource(R.drawable.blank_checkbox)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            // 하트 누르기
            comdetail_heart.setOnClickListener {
                onHeartClicked(com_postId)
            }

            // 승인 버튼 누르기
            comdetail_accept.setOnClickListener {
                onAuthClicked(com_postId)
                Toast.makeText(this,"승인하셨습니다.", Toast.LENGTH_SHORT).show()
                comdetail_text1.visibility = View.GONE
                comdetail_accept.visibility = View.GONE
                comdetail_deny.visibility = View.GONE
            }

            // 거부 버튼 누르기
            comdetail_deny.setOnClickListener {
                Toast.makeText(this,"거부하셨습니다.", Toast.LENGTH_SHORT).show()
                comdetail_text1.visibility = View.GONE
                comdetail_accept.visibility = View.GONE
                comdetail_deny.visibility = View.GONE
            }



        }


    }

    private fun onAuthClicked(postId: String) {
        var uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var postRef = Firebase.database.getReference("community").child(postId)


        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)
                        ?: return Transaction.success(mutableData)

                if (p.auths.containsKey(uid)) {

                    // Unstar the post and remove self from stars
                    p.authCount = p.authCount?.minus(1)
                    p.auths.remove(uid)
                } else {
                    // Star the post and add self to stars
                    p.authCount = p.authCount?.plus(1)
                    p.auths[uid] = true
                }

                // Set value and report transaction success
                mutableData.value = p
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
            ) {
                // Transaction completed
                Log.d("TAG", "postTransaction:onComplete:" + databaseError!!)
            }
        })
    }



    private fun onHeartClicked(postId: String) {
        var uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var postRef = Firebase.database.getReference("community").child(postId)


        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)
                        ?: return Transaction.success(mutableData)

                if (p.hearts.containsKey(uid)) {
                    // Unstar the post and remove self from stars
                    p.heartCount = p.heartCount?.minus(1)
                    p.hearts.remove(uid)
                } else {
                    // Star the post and add self to stars
                    p.heartCount = p.heartCount?.plus(1)
                    p.hearts[uid] = true
                }

                // Set value and report transaction success
                mutableData.value = p
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
            ) {
                // Transaction completed
                Log.d("TAG", "postTransaction:onComplete:" + databaseError!!)
            }
        })
    }
}