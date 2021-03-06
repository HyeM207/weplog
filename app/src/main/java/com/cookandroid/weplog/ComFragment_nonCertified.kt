package com.cookandroid.weplog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ComFragment_nonCertified: Fragment() {

    private lateinit var adapter: PostListViewAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel_noncertified::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.community, container, false)

        // 리사이클러뷰 어댑터에 화면에 띄울 데이터를 넘긴다.
        adapter = PostListViewAdapter(requireContext())


        var rv_postList: RecyclerView = view.findViewById<RecyclerView>(R.id.com_postList)

        // 역순 출력
        var manager = LinearLayoutManager (requireContext())
        manager.reverseLayout = true
        manager.stackFromEnd = true

        rv_postList.layoutManager = manager
        rv_postList.setHasFixedSize(true)
        //rv_postList.adapter = PostListViewAdapter(mutableData)
        rv_postList.adapter = adapter
        observerData()

        return view
    }

    private fun observerData() {
        viewModel.fetchData().observe(viewLifecycleOwner, Observer {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }



}