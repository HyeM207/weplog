package com.cookandroid.weplog

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Record (
    var distance : String ?= null,
    var time : String ?= null
){
    /*
       data class TmpRecord(
         var startPhoto : String ?= null,
         var endPhoto : String ?= null,
         var endQr : String ?= null
       )


       data class Record(
4
       )
     */

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "distance" to distance,
            "time" to time
        )
    }



}
