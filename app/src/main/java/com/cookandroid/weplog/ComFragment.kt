package com.cookandroid.weplog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ComFragment : Fragment() {


    private lateinit var adapter: PostListViewAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(ListViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view =  inflater.inflate(R.layout.community, container, false)


//        val postList : ArrayList<Post>
//        postList = ArrayList()
        val mutableData = MutableLiveData<MutableList<Post>>()

        val postRef = Firebase.database.getReference("community")
        postRef.addValueEventListener(object : ValueEventListener{
            val postList : MutableList<Post> = mutableListOf<Post>()
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val getData = userSnapshot.getValue(Post::class.java)
                        postList.add(getData!!)

                        mutableData.value = postList
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

//        var imsiPost = Post()
//        imsiPost.authNum = 2
//        imsiPost.certified = false
//        imsiPost.heartNum = 5
//        imsiPost.numberReport = 0
//        imsiPost.postId = "postIdBlaBla"
//        imsiPost.writerId =  "sdfsdgni3qoi2b3"
//        imsiPost.writerNick ="Test"
//
//
//        postList.add(imsiPost)
//        postList.add(imsiPost)
//        postList.add(imsiPost)
//        postList.add(imsiPost)


        // 리사이클러뷰 어댑터에 화면에 띄울 데이터를 넘긴다.
        adapter = PostListViewAdapter(requireContext())


        var rv_postList: RecyclerView = view.findViewById<RecyclerView>(R.id.com_postList)
        rv_postList.layoutManager = LinearLayoutManager (requireContext())
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