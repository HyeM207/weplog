package com.cookandroid.weplog

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    var uid : String ?= "",
    var email : String ?= "",
    var password : String ?= "",
    var nickname : String ?= "",
    var phone : String ?= "",
    var grade : Int ?= 0,
    var credit : Int ?= 0
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
            "credit" to credit
            )
    }



}
