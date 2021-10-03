package com.cookandroid.weplog

import com.google.firebase.database.Exclude
import java.sql.Timestamp

class Post (
        var postId : String ?= "", // 글 고유 id
        var writerId : String ?= "", // 작성자 id
        var writerNick : String ?= "", // 작성자 nickname
        var photoUrl : String ?= "", // 사진 url
        var timestamp: Long ?= null, // 글 업로드 시간


        var authCount : Int ?= 0, // 승인 받은 횟수
        var certified : Boolean ?= authCount!! >2 , // 승인 여부 (승인 횟수가 3회 이상일 때만 true)
        var auths :  MutableMap<String, Boolean> = HashMap(),  // 승인 여부 목록

        var heartCount : Int ?= 0, // 좋아요 수
        var hearts : MutableMap<String, Boolean> = HashMap(),  // 좋아요 한 목록

        var isView :  Boolean ?= true, // 게시되는 여부
        var reportCnt : Int ?= 0, // 신고 횟수
        var reports :  MutableMap<String, String> = HashMap()  // 신고 항목 (신고자 uid, 신고 항목)

)
{
    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
                "postId" to postId,
                "writerId" to writerId,
                "writerNick" to writerNick,
                "photoUrl" to photoUrl,
                "authCount" to authCount,
                "auths" to auths,
                "certified" to certified,
                "timestamp" to timestamp,
                "heartCount" to heartCount,
                "hearts" to hearts,
                "reportCnt" to reportCnt,
                "reports" to reports,
                "isView" to isView

        )
    }
}
