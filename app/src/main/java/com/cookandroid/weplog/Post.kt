package com.cookandroid.weplog

import com.google.firebase.database.Exclude
import java.sql.Timestamp

class Post (
             var postId : String ?= "", // 글 고유 id
             var writerId : String ?= "", // 작성자 id
             var writerNick : String ?= "", // 작성자 nickname
             var photoUrl : String ?= "", // 사진 url

             var heartNum : Int ?= 0, // 좋아요 수
             var authNum : Int ?= 0, // 승인 받은 횟수
             var certified : Boolean ?= false, // 승인 여부
             var numberReport : Int ?= 0, // 신고 횟수
             var timestamp: Long ?= null // 글 업로드 시간
)
{
    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "postId" to postId,
            "writerId" to writerId,
            "writerNick" to writerNick,
            "photoUrl" to photoUrl,
            "heartNum" to heartNum,
            "authNum" to authNum,
            "certified" to certified,
            "numberReport" to numberReport,
            "timestamp" to timestamp
        )
    }
}
