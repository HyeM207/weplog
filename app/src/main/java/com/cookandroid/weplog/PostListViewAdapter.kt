package com.cookandroid.weplog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

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

        // holder.comitem_nick.text = postList.get(position).writerNick
        holder.comitem_nick.text = postList[position].writerNick.toString()

        // item 클릭 시
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView?.context, ComDetail::class.java)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }
    override fun getItemCount(): Int {
        return postList.size
    }


    class CustomViewHolder(itemView : View): RecyclerView.ViewHolder(itemView) {
        val comitem_nick = itemView.findViewById<TextView>(R.id.comitem_nick)
        val comitem_photo = itemView.findViewById<ImageView>(R.id.comitem_photo)
    }

}
