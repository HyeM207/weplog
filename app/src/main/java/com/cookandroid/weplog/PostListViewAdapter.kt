package com.cookandroid.weplog

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceControl
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat

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
        var database = Firebase.database.reference

        // 닉네임 표기
        holder.comitem_nick.text = postList[position].writerNick.toString()


        // 사진 불러오기
        Firebase.storage.reference.child("community").child(postList[position].photoUrl.toString()).downloadUrl.addOnCompleteListener {
            if(it.isSuccessful){
                Glide.with(holder.itemView?.context)
                        .load(it.result)
                        .placeholder(R.drawable.loading2)
                        .into(holder.comitem_photo)
            }
        }


        // 사용자 grade별 사진 출력
        database.child("users").child(postList[position].writerId.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val grade = snapshot.child("grade").value.toString()
                when(grade){
                    "1"-> holder.comitem_profile.setImageResource(R.drawable.yellow_circle)
                    "2"-> holder.comitem_profile.setImageResource(R.drawable.green2_circle)
                    "3"-> holder.comitem_profile.setImageResource(R.drawable.blue2_circle)
                    "4"-> holder.comitem_profile.setImageResource(R.drawable.red_circle)
                    "5"-> holder.comitem_profile.setImageResource(R.drawable.purple2_circle)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        })


        var uid = FirebaseAuth.getInstance().currentUser?.uid


        // 하트 표기
        if (postList[position].hearts.containsKey(uid)){
            holder.comitem_heart.setImageResource(R.drawable.greenheart)
        }else{
            holder.comitem_heart.setImageResource(R.drawable.greennoheart)
        }

        // 하트 수 출력
        holder.comitem_heartCount.text = postList[position].heartCount.toString()


        // timestamp 표기 (timestamp -> Date)
        holder.comitem_timestamp.text = SimpleDateFormat("yyyy.MM.dd hh:mm").format(postList[position].timestamp).toString()

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
            if (postList[position]?.auths?.get(uid) == true) {
                holder.comitem_check.setImageResource(R.drawable.green_checkbox)
            } else {
                holder.comitem_check.setImageResource(R.drawable.green_cross_checkbox)
            }
        } else {
            holder.comitem_check.setImageResource(R.drawable.green_blank_checkbox)
        }



    }


    override fun getItemCount(): Int {
        return postList.size
    }


    class CustomViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {
        val comitem_nick = itemView.findViewById<TextView>(R.id.comitem_nick)
        val comitem_photo = itemView.findViewById<ImageView>(R.id.comitem_photo)
        val comitem_heart = itemView.findViewById<ImageView>(R.id.comitem_heart)
        val comitem_check = itemView.findViewById<ImageView>(R.id.comitem_check)
        val comitem_heartCount = itemView.findViewById<TextView>(R.id.comitem_heartCount)
        val comitem_authNum = itemView.findViewById<TextView>(R.id.comitem_authNum)
        val comitem_timestamp = itemView.findViewById<TextView>(R.id.comitem_timestamp)
        val comitem_profile = itemView.findViewById<ImageView>(R.id.comitem_profile)
    }

}

