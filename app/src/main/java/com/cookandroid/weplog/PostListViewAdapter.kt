package com.cookandroid.weplog

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostListViewAdapter(private val context: Context): RecyclerView.Adapter <PostListViewAdapter.CustomViewHolder>(){
    private var postList =  mutableListOf<Post>()

    fun setListData(data:MutableList<Post>){
        postList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListViewAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comitem,parent,false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostListViewAdapter.CustomViewHolder, position: Int) {


        // 닉네임 표기
        holder.comitem_nick.text = postList[position].writerNick.toString()


        // 사진 불러오기
        Firebase.storage.reference.child("community").child(postList[position].photoUrl.toString()).downloadUrl.addOnCompleteListener {
            if(it.isSuccessful){
                Glide.with(holder.itemView?.context)
                        .load(it.result)
                        .into(holder.comitem_photo)
            }
        }


        var uid = FirebaseAuth.getInstance().currentUser?.uid


        // 하트 표기
        if (postList[position].hearts.containsKey(uid)){
            holder.comitem_heart.setImageResource(R.drawable.heart)
        }else{
            holder.comitem_heart.setImageResource(R.drawable.noheart)
        }

        // 하트 수 출력
        holder.comitem_heartCount.text = postList[position].heartCount.toString()


        // 하트 클릭시
        holder.comitem_heart.setOnClickListener {
            onHeartClicked(position)
        }

        // 인증 여부 표기
        if (postList[position].certified == true){
            holder.comitem_check.setImageResource(R.drawable.checkbox)
        }else{
            holder.comitem_check.setImageResource(R.drawable.blank_checkbox)
        }

        // 인증 수 표기
        holder.comitem_authNum.text = postList[position].authCount.toString()

        // item 클릭 시
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView?.context, ComDetail::class.java)
            intent.putExtra("com_postId",  postList[position].postId)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }

        // 승인/거절한 게시물은 표시 x and 이미지 변경
        if (postList[position]?.auths?.containsKey(uid) == true) {
            if (postList[position]?.auths?.get(uid) == true){
                holder.comitem_check.setImageResource(R.drawable.checkbox)
            }else{
                holder.comitem_check.setImageResource(R.drawable.cross_checkbox)
            }
        }else{
            holder.comitem_check.setImageResource(R.drawable.blank_checkbox)
        }

    }

    private fun onHeartClicked(position : Int) {

        var uid : String = FirebaseAuth.getInstance().currentUser?.uid.toString()
        var postId : String = postList[position].postId.toString()
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

    override fun getItemCount(): Int {
        return postList.size
    }


    class CustomViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {
        val comitem_nick = itemView.findViewById<TextView>(R.id.history_date)
        val comitem_photo = itemView.findViewById<ImageView>(R.id.comitem_photo)
        val comitem_heart = itemView.findViewById<ImageView>(R.id.comitem_heart)
        val comitem_check = itemView.findViewById<ImageView>(R.id.comitem_check)
        val comitem_heartCount = itemView.findViewById<TextView>(R.id.comitem_heartCount)
        val comitem_authNum = itemView.findViewById<TextView>(R.id.comitem_authNum)

    }

}

