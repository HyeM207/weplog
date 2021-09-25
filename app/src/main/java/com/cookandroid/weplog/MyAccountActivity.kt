package com.cookandroid.weplog

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.mypage_account.*

class MyAccountActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mypage_account)

        val items= mutableListOf<ListViewItem>()

        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_article_24), "개인정보 수정"))
        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_power_settings_new_24), "로그아웃"))
        items.add(ListViewItem(ContextCompat.getDrawable(this, R.drawable.ic_baseline_block_24), "회원탈퇴"))


        val adapter=ListViewAdapter(items)
        my_accountlist.adapter=adapter

        my_accountlist.setOnItemClickListener{ parent : AdapterView<*>, view: View, position:Int, id:Long ->
            val item=parent.getItemAtPosition(position) as ListViewItem
            Toast.makeText(this, item.title, Toast.LENGTH_SHORT).show()

            if (item.title=="개인정보 수정"){
                val profileIntent= Intent(this, MyProfileActivity::class.java)
                startActivity(profileIntent)
            }else if (item.title=="로그아웃"){
                Firebase.auth.signOut()
                var intent = Intent(this, Login::class.java)
                startActivity(intent)
            }


        }


    }
}