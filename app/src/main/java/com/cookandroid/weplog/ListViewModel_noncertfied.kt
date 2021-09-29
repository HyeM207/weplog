package com.cookandroid.weplog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListViewModel_noncertified  : ViewModel() {
    private val repo = Repo_noncertified()
    fun fetchData(): LiveData<MutableList<Post>> {
        val mutableData = MutableLiveData<MutableList<Post>>()
        repo.getData().observeForever{
            mutableData.value = it
        }
        return mutableData
    }
}