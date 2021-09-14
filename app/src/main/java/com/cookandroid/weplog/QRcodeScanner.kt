package com.cookandroid.weplog

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class QRcodeScanner : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qrcode_scanner)

        initQRcodeScanner()
    }

    private fun initQRcodeScanner() {
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
                //var toast = Toast(this)
               // toast.initQrcodeToast(result.contents)
                Toast.makeText(this, result.contents.toString(), Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}