package com.cookandroid.weplog

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import kotlinx.android.synthetic.main.mypage.*
import kotlinx.android.synthetic.main.mypage.view.*


class MypageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.mypage, container, false)

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