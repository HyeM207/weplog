package com.cookandroid.weplog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat

class MyPostListViewAdapter (private val context: Context): RecyclerView.Adapter <MyPostListViewAdapter.CustomViewHolder>(){
    private var postList =  mutableListOf<Post>()

    fun setListData(data:MutableList<Post>){
        postList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostListViewAdapter.CustomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mypost_item,parent,false)
        return CustomViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyPostListViewAdapter.CustomViewHolder, position: Int) {


        // 사진 불러오기
        Firebase.storage.reference.child("community")
            .child(postList[position].photoUrl.toString()).downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(holder.itemView?.context)
                    .load(it.result)
                    .placeholder(R.drawable.loading2)
                    .into(holder.myPostitem_photo)
            }
        }

        // item 클릭 시
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView?.context, ComDetail::class.java)
            intent.putExtra("com_postId",  postList[position].postId)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }

    }

    override fun getItemCount(): Int {
        return postList.size
    }


    class CustomViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {
        val myPostitem_photo = itemView.findViewById<ImageView>(R.id.myPostitem_photo)

    }

}

