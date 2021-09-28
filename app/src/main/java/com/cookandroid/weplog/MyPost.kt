package com.cookandroid.weplog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyPost : AppCompatActivity() {

    private lateinit var adapter: MyPostListViewAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel_MyPost::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypost)


        // 리사이클러뷰 어댑터에 화면에 띄울 데이터를 넘긴다.
        adapter = MyPostListViewAdapter(this)


        var rv_postList: RecyclerView = findViewById<RecyclerView>(R.id.mypost_postList)

        // 역순 출력
        var manager = GridLayoutManager (this,3)

        rv_postList.layoutManager = manager
        rv_postList.setHasFixedSize(true)
        rv_postList.adapter = adapter
        observerData()

    }

    private fun observerData() {
        viewModel.fetchData().observe(this, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }
}
