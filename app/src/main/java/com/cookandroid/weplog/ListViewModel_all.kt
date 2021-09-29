package com.cookandroid.weplog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel_all  : ViewModel() {
    private val repo = Repo_all()
    fun fetchData(): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        repo.getData().observeForever{
            mutableData.value = it
        }
        return mutableData
    }
}