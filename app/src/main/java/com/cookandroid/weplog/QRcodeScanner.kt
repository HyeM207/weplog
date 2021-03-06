package com.cookandroid.weplog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRcodeScanner : AppCompatActivity() {

    // private val MapFragment by lazy { MapFragment() }

    lateinit var pageName : String
    lateinit var pushRefKey : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qrcode_scanner)


        if (intent.hasExtra("page")){
            pageName = intent.getStringExtra("page").toString()
            Toast.makeText(this, pageName +  "페이지 이름", Toast.LENGTH_SHORT).show()

            if (intent.hasExtra("pushRefKey")){
                pushRefKey = intent.getStringExtra("pushRefKey").toString()
            }

            initQRcodeScanner(pageName)
        }



    }

    private fun initQRcodeScanner(pageName : String) {
        val integrator  = IntentIntegrator(this)
        integrator.setBeepEnabled(false)  // 소리 설정
        integrator.setOrientationLocked(true) // 세로 가로 모드 고정
        integrator.setPrompt("QR코드를 인증해주세요.")
        integrator.initiateScan() // QR 코드 스캐너  보여지고, 결과값은 onActivityResult로 전달됨
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result : IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if(result !=null) {
            if(result.contents == null) {
                Toast.makeText(this, "QR코드 인증이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // 페이지 이동
                if (pageName.equals("Authentication")) {
                    var intent = Intent(this, Authentication::class.java)
                    intent.putExtra("trashplace", result.contents.toString())
                    intent.putExtra("pushRefKey", pushRefKey)
                    Log.e("1002", pushRefKey+"QR 페이지에서 보내려고 함")
                    //Toast.makeText(this, result.contents.toString() + "내용", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}