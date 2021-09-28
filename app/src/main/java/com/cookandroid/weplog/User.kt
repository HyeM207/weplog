package com.cookandroid.weplog

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
        var uid: String ?= "",
        var email: String ?= "",
        var password: String ?= "",
        var nickname: String ?= "",
        var phone: String ?= "",
        var credit: Int ?= 0,
        var grade: Int ?= 1,
        var todayAuth: Boolean = false, // 일 인증 횟수 1회로 제한
        var joindate: String ?= "",
        //var posts : String ?= ""
        var posts:  MutableMap<String, Boolean> = HashMap(),  // 게시물 목록

){
    /*
       data class TmpRecord(
         var startPhoto : String ?= null,
         var endPhoto : String ?= null,
         var endQr : String ?= null
       )


       data class Record(

       )
     */

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
                "uid" to uid,
                "email" to email,
                "password" to password,
                "nickname" to nickname,
                "phone" to phone,
                "grade" to grade,
                "credit" to credit,
                "todayAuth" to todayAuth,
                "joindate" to joindate,
                "posts" to posts
        )
    }



}
