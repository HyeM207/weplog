package com.cookandroid.weplog

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class History (
    var year : String ?= null,
    var month : String ?= null,
    var day : String ?= null,
    var distance : String ?= null,
    var time : String ?= null,
    var endTime : String ?= null,
    var startTime : String ?= null

){

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "year" to year,
            "month" to month,
            "day" to day,
            "distance" to distance,
            "time" to time,
            "endTime" to endTime,
            "startTime" to startTime
        )
    }

//    fun History(month: String?, day: String?, distance: String?, time: String?, endTime: String?, startTime: String?){
//
//    }



}
