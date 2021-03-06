package com.cookandroid.weplog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel_MyPost: ViewModel() {
    private val repo = Repo_Mypost()
    fun fetchData(): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        repo.getData().observeForever{
            mutableData.value = it
        }
        return mutableData
    }
}