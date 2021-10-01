package com.cookandroid.weplog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.mypage.*
import kotlinx.android.synthetic.main.mypage.view.*


class MypageFragment : Fragment() {
    lateinit var my_nick : TextView
    lateinit var my_lv : ImageView
    lateinit var my_credit : TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.mypage, container, false)

        my_nick = view.findViewById(R.id.my_nick)
        my_lv = view.findViewById(R.id.my_lv)
        my_credit = view.findViewById(R.id.my_credit)

        // nickname & grade 이미지 & credit 불러오기
        val user = Firebase.auth.currentUser
        val userRef = Firebase.database.getReference("users")

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // nickname 설정
                val name = snapshot.child(user?.uid.toString()).child("nickname").value
                my_nick.setText(name.toString())


                // grade 이미지 설정
                val grade = snapshot.child(user?.uid.toString()).child("grade").value.toString()
                when(grade){
                    "1"-> my_lv.setImageResource(R.drawable.yellow_circle)
                    "2"-> my_lv.setImageResource(R.drawable.green2_circle)
                    "3"-> my_lv.setImageResource(R.drawable.blue2_circle)
                    "4"-> my_lv.setImageResource(R.drawable.red_circle)
                    "5"-> my_lv.setImageResource(R.drawable.purple2_circle)
                }

                // credit 불러오기
                val credit = snapshot.child(user?.uid.toString()).child("credit").value.toString()
                my_credit.text = credit + " 크레딧"

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        })



        val items = mutableListOf<ListViewItem>()
        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_account_circle_24, null), "내가 쓴 글"))
        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_vpn_key_24, null)!!, "계정관리"))
        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_announcement_24, null)!!, "공지사항"))
        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_contact_support_24, null)!!, "문의하기"))
        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_notifications_none_24, null)!!, "푸시알림 설정"))
        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_article_24, null)!!, "about"))
//        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_power_settings_new_24, null)!!, "로그아웃"))
//        items.add(ListViewItem(ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.ic_baseline_block_24, null)!!, "회원탈퇴"))


        val adapter = ListViewAdapter(items)

        view.my_list.adapter=adapter


        view.my_list.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
            val item = parent.getItemAtPosition(position) as ListViewItem
            Toast.makeText(activity, item.title, Toast.LENGTH_SHORT).show()

            if (item.title=="계정관리"){
                val accountIntent=Intent(activity, MyAccountActivity::class.java)
                startActivity(accountIntent)
            }

            if (item.title=="내가 쓴 글"){
                var intent = Intent(activity, MyPost::class.java)
                startActivity(intent)
            }





        }






        return view
    }






}