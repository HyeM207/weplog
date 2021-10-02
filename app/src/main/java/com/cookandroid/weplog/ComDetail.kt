package com.cookandroid.weplog

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat


class ComDetail : AppCompatActivity() {
    lateinit var comdetail_nick : TextView
    lateinit var comdetail_photo : ImageView
    lateinit var comdetail_text1 : TextView
    lateinit var comdetail_accept : Button
    lateinit var comdetail_deny : Button
    lateinit var comdetail_heart : ImageView
    lateinit var comdetail_check : ImageView
    lateinit var comdetial_certified : TextView
    lateinit var comdetail_timestamp : TextView
    lateinit var comdetail_more : ImageView
    lateinit var comdetail_profile : ImageView
    lateinit var comdetail_heartCount : TextView
    lateinit var comdetail_constLayout2 : ConstraintLayout
    lateinit var comdetail_text0 : TextView

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
        comdetail_timestamp = findViewById(R.id.comdetail_timestamp)
        comdetail_more = findViewById(R.id.comdetail_more)
        comdetail_profile = findViewById(R.id.comdetail_profile)
        comdetail_heartCount = findViewById(R.id.comdetail_heartCount)
        comdetail_constLayout2 = findViewById(R.id.comdetail_constLayout2)
        comdetail_text0 = findViewById(R.id.comdetail_text0)

        // postId 가져옴
        val intent = intent
        val com_postId = intent.getStringExtra("com_postId") // intent로 받아온 postId

        val user = Firebase.auth.currentUser
        var database = Firebase.database.reference


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
                            Firebase.storage.reference.child("community")
                                    .child(it).downloadUrl.addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            if (!this@ComDetail.isDestroyed) {  // 오류 해결
                                                Glide.with(this@ComDetail)
                                                        .load(it.result)
                                                        .placeholder(R.drawable.loading2)
                                                        .into(comdetail_photo)

                                            }
                                        }
                                    }
                        }

                        // grade별 프로필 설정
                        database.child("users").child(getData?.writerId.toString())
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val grade = snapshot.child("grade").value.toString()
                                        when (grade) {
                                            "1" -> comdetail_profile.setImageResource(R.drawable.yellow_circle)
                                            "2" -> comdetail_profile.setImageResource(R.drawable.green2_circle)
                                            "3" -> comdetail_profile.setImageResource(R.drawable.blue2_circle)
                                            "4" -> comdetail_profile.setImageResource(R.drawable.red_circle)
                                            "5" -> comdetail_profile.setImageResource(R.drawable.purple2_circle)
                                        }

                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Getting Post failed, log a message
                                    }
                                })


                        // 하트 수 출력
                        comdetail_heartCount.text = getData?.heartCount.toString()


                        // 자신의 글은 승인 못함
                        if (user?.uid.toString().equals(getData?.writerId.toString())) {
                            comdetail_text0.visibility = View.GONE
                            comdetail_text1.visibility = View.GONE
                            comdetail_accept.visibility = View.GONE
                            comdetail_deny.visibility = View.GONE
                            comdetail_constLayout2.visibility = View.GONE
                        }

                        // 인증 받은 글이면 visible 표기
                        if (getData?.certified == true) {
                            comdetial_certified.visibility = View.VISIBLE
                        }
                        else{
                            comdetial_certified.visibility = View.INVISIBLE
                        }


                        // timestamp 표기 (timestamp -> Date)
                        comdetail_timestamp.text =
                                SimpleDateFormat("yyyy.MM.dd hh:mm").format(getData?.timestamp)
                                        .toString()


                        // 하트 표기
                        if (getData?.hearts?.containsKey(uid) == true) {
                            comdetail_heart.setImageResource(R.drawable.greenheart)
                        } else {
                            comdetail_heart.setImageResource(R.drawable.greennoheart)
                        }


                        // 승인/거절한 게시물은 표시 x and 이미지 변경
                        if (getData?.auths?.containsKey(uid) == true) {
                            comdetail_text0.visibility = View.GONE
                            comdetail_text1.visibility = View.GONE
                            comdetail_accept.visibility = View.GONE
                            comdetail_deny.visibility = View.GONE
                            comdetail_constLayout2.visibility = View.GONE
                            if (getData?.auths?.get(uid) == true) {
                                comdetail_check.setImageResource(R.drawable.green_checkbox)
                            } else {
                                comdetail_check.setImageResource(R.drawable.green_cross_checkbox)
                            }
                        } else {
                            comdetail_check.setImageResource(R.drawable.green_blank_checkbox)
                            comdetail_text0.text = getData?.writerNick.toString() + " 님의"
                        }


                        // 더보기 버튼
                        comdetail_more.setOnClickListener {
                            var popup = PopupMenu(this@ComDetail, it)


                            // 본인일 경우
                            if (user?.uid.toString().equals(getData?.writerId.toString())) {
                                menuInflater.inflate(R.menu.menu_comdetail_user, popup.menu)
                                popup.show()
                                popup.setOnMenuItemClickListener {
                                    when (it.itemId) {
                                        R.id.itemRefresh -> {
                                            Refresh()
                                            return@setOnMenuItemClickListener true
                                        }
                                        R.id.itemDelete-> {

                                            var selectedItem: String? = null
                                            val builder = AlertDialog.Builder(this@ComDetail)
                                                    .setTitle("게시물 삭제")
                                                    .setMessage("게시물 삭제 시 복구 불가능합니다. 삭제하시겠습니까?")
                                                    .setPositiveButton("삭제", DialogInterface.OnClickListener { dialog, which ->

                                                        // storage에 있는 사진 삭제
                                                        getData?.photoUrl?.let { it1 ->
                                                            Firebase.storage.reference.child("community/$it1").delete().addOnSuccessListener {
                                                                Log.e("delete", "삭제 성공!")
                                                            }.addOnFailureListener {
                                                                Log.e("delete", "삭제 실패!"+it1)
                                                            }
                                                        }

                                                        // community DB 게시물 삭제
                                                        var postRef = getData?.postId?.let { it1 ->
                                                            Firebase.database.getReference("community").child(it1)
                                                        }
                                                        if (postRef != null) {
                                                            postRef.setValue(null)
                                                        }

                                                        // user DB 게시물 삭제
                                                        var userRef =  Firebase.database.getReference("users").child(user?.uid.toString()).child("posts").child(getData?.postId.toString())
                                                        userRef.setValue(null)

                                                        var intent = Intent(this@ComDetail, NavigationActivity::class.java)
                                                        startActivity(intent)
                                                    })
                                                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                                                    })
                                                    .show()
                                            return@setOnMenuItemClickListener true
                                        }
                                        else -> {
                                            return@setOnMenuItemClickListener false
                                        }
                                    }
                                }
                            } else {  // 타인일 경우
                                menuInflater.inflate(R.menu.menu_comdetail_others, popup.menu)
                                popup.show()
                                popup.setOnMenuItemClickListener {
                                    when (it.itemId) {
                                        R.id.itemRefresh -> {
                                            Refresh()
                                            return@setOnMenuItemClickListener true
                                        }

                                        R.id.itemReport -> {
                                            val items = arrayOf(
                                                    "음란물 및 유해 콘텐츠",
                                                    "증오 혹은 악의적인 콘텐츠",
                                                    "욕설 및 비하",
                                                    "유출/사칭/사기",
                                                    "상업적 광고 및 판매"
                                            )
                                            var selectedItem: String? = null
                                            val builder = AlertDialog.Builder(this@ComDetail)
                                                    .setTitle("신고 항목")
                                                    .setSingleChoiceItems(items, -1) { dialog, which ->
                                                        selectedItem = items[which]

                                                    }
                                                    .setPositiveButton("신고") { dialog, which ->
                                                        Log.e("reports", "신고 함수 호출 전" + selectedItem)
                                                        selectedItem?.let {
                                                            reportPost(
                                                                    com_postId.toString(),
                                                                    it
                                                            )
                                                        }
                                                    }
                                                    .setNegativeButton(
                                                            "취소",
                                                            DialogInterface.OnClickListener { dialog, which -> })
                                                    .show()
                                            return@setOnMenuItemClickListener true
                                        }

                                        else -> {
                                            return@setOnMenuItemClickListener false
                                        }
                                    }

                                }
                            }

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
                Toast.makeText(this, "승인 완료.", Toast.LENGTH_SHORT).show()
                comdetail_text1.visibility = View.GONE
                comdetail_accept.visibility = View.GONE
                comdetail_deny.visibility = View.GONE
            }

            // 취소 버튼 누르기
            comdetail_deny.setOnClickListener {
                Toast.makeText(this, "취소", Toast.LENGTH_SHORT).show()
                comdetail_text1.visibility = View.GONE
                comdetail_accept.visibility = View.GONE
                comdetail_deny.visibility = View.GONE
            }






        }

    }

    private fun Refresh() {
        try {
            val intent = getIntent()
            finish() //현재 액티비티 종료 실시
            overridePendingTransition(0, 0) //인텐트 애니메이션 없애기
            startActivity(intent) //현재 액티비티 재실행 실시
            overridePendingTransition(0, 0) //인텐트 애니메이션 없애기

            Log.e("Spinner", "새로고침 완료")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Spinner", "새로고침 실패")
        }
    }

    private fun reportPost(postId : String, reportTitle : String){
        var toastM : String ?= ""

        var uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var postRef = Firebase.database.getReference("community").child(postId)
        var database = Firebase.database.reference


        //Log.e("reports", "신고 함수 안")
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)

                if (p == null) {
                    Log.e("reports", "p 가 null")
                    return Transaction.success(mutableData)
                } else {
                    if(p.reportCnt!! >= 3){
                        Log.e("reports", "이미 신고처리 중인 게시물입니다.")
                        toastM = "이미 신고처리 중인 게시물입니다."
                    }else if (p.reports.containsKey(uid)) {
                        Log.e("reports", "이미 신고함")
                        toastM = "이미 신고하셨습니다."
                        //Toast.makeText(this@ComDetail, "이미 신고하셨습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        p.reportCnt = p.reportCnt?.plus(1)
                        p.reports[uid] = reportTitle
                        //Log.e("reports", "신고 완료")
                        toastM = "신고 완료"

                        if (p.reportCnt!! == 3){
                                p.isView = false  // 안 보이게 설정
//                            // 데이터 복제
//                            var post = Post()
//
//                            post.postId = p.postId
//                            post.writerId = p.writerId
//                            post.timestamp = p.timestamp
//                            post.reportCnt = p.reportCnt
//                            post.writerNick = p.writerNick
//                            post.photoUrl = p.photoUrl
//                            post.heartCount = p.heartCount
//                            post.certified = p.certified
//                            post.authCount = p.authCount
//                            post.auths = p.auths
//                            post.reports = p.reports
//
//                            var postValues : Map<String, Any?> = post.toMap()
//                            p.postId?.let { database.child("reports").child(it).setValue(postValues) }
//
//                            // 데이터 삭제
//                            postRef.setValue(null)
                        }

                    }
                }
                Log.e("reports", "여기는 왔니?")
                // Set value and report transaction success
                mutableData.value = p
                return Transaction.success(mutableData)
            }
            override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
            ) {
                if (databaseError != null) {
                    Log.e("reports", "안녕")
                    Toast.makeText(this@ComDetail, toastM, Toast.LENGTH_SHORT).show()
                }
            }
        })


    }


    private fun onAuthClicked(postId: String) {
        var uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var postRef = Firebase.database.getReference("community").child(postId)
        var database = Firebase.database.reference



        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)


                if (p == null) {
                    Log.e("reports", "p 가 null")
                    return Transaction.success(mutableData)
                } else {
                    if (!p.auths.containsKey(uid)) {
                        // Star the post and add self to stars
                        p.authCount = p.authCount?.plus(1)
                        p.auths[uid] = true


                        // 크레딧 지급 (5 credit)
                        if (p.authCount!! == 3) {
                            p.certified = true
                            database.child("users").child(p.writerId!!)
                                .child("posts/${p.postId}").setValue(true)

                            database.child("users").child(p.writerId!!).get()
                                .addOnSuccessListener {
                                    var credit: Int =
                                        it.child("credit").value.toString().toInt()
                                    credit += 5

                                    var grade = 0
                                    if (credit!! >= 200) {
                                        grade = 5
                                    } else {
                                        if (credit!! >= 100) {
                                            grade = 4
                                        } else
                                            if (credit!! >= 50) {
                                                grade = 3
                                            } else
                                                if (credit!! >= 20) {
                                                    grade = 2
                                                } else {
                                                    grade = 1
                                                }
                                    }
                                    database.child("users").child(p.writerId!!)
                                        .child("credit")
                                        .setValue(credit.toString())
                                    database.child("users").child(p.writerId!!)
                                        .child("grade")
                                        .setValue(grade.toString())
                                }

                            Log.e("인증", "인증 크레딧 완료")
                        }
                    }
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
                //Log.d("TAG", "postTransaction:onComplete:" + databaseError!!)
            }

        })




    }




    private fun onHeartClicked(postId: String) {
        var uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var postRef = Firebase.database.getReference("community").child(postId)


        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)

                if (p == null) {
                    return Transaction.success(mutableData)
                } else {
                    if (p.hearts.containsKey(uid)) {
                        // Unstar the post and remove self from stars
                        p.heartCount = p.heartCount?.minus(1)
                        p.hearts.remove(uid)
                    } else {
                        // Star the post and add self to stars
                        p.heartCount = p.heartCount?.plus(1)
                        p.hearts[uid] = true
                    }
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
                //  Log.d("TAG", "postTransaction:onComplete:" + databaseError!!)
            }
        })
    }


}