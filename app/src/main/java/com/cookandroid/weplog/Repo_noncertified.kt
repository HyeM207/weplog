package com.cookandroid.weplog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class Repo_noncertified {
    fun getData(): LiveData<MutableList<Post>> {
        val user = Firebase.auth.currentUser
        val mutableData = MutableLiveData<MutableList<Post>>()
        val database = Firebase.database
        val postRef = Firebase.database.getReference("community")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val postList: MutableList<Post> = mutableListOf<Post>()
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val getData = userSnapshot.getValue(Post::class.java)
                        if (getData?.isView == true) {
                            if (getData?.certified == false) { // 인증된 글만
                                postList.add(getData!!)
                            }
                        }
                        mutableData.value = postList
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return mutableData
    }
}
