package com.cookandroid.weplog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ComFragment : Fragment() {

    lateinit var viewPagers: ViewPager2
    lateinit var tabLayouts: TabLayout


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        var view  = inflater.inflate(R.layout.community_tab, container, false)
        viewPagers = view.findViewById(R.id.com_viewPager)
        tabLayouts = view.findViewById(R.id.com_tab)
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 3개의 fragment add
        val pagerAdapter = ComPagerAdapter(requireActivity())
        pagerAdapter.addFragment(ComFragment_all())
        pagerAdapter.addFragment(ComFragment_nonCertified())
        pagerAdapter.addFragment(ComFragment_certified())



        // adapter 연결
        viewPagers.adapter = pagerAdapter
        viewPagers?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            }
        })



        // tablayout 연결
        TabLayoutMediator(tabLayouts, viewPagers){ tab, position ->
            when(position){
                0 ->     tab.text = "전체"
                1 ->     tab.text = "미인증글"
                2 ->     tab.text = "인증글"
            }


        }.attach()
    }

}