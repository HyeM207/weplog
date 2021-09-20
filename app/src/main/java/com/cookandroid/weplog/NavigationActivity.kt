package com.cookandroid.weplog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class NavigationActivity : AppCompatActivity() {

    private val MainFragment by lazy { MainFragment() }
    private val MapFragment by lazy { MapFragment() }
    private val ComFragment by lazy { ComFragment() }
    private val MypageFragment by lazy { MypageFragment() }


    lateinit var bottomNavigation: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        bottomNavigation=findViewById(R.id.bottomNavigationView1)
        initNavigationBar()



    }

    private fun initNavigationBar() {
        bottomNavigation.run {
            setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.menu_home -> {
                        changeFragment(MainFragment)
                    }
                    R.id.menu_map -> {
                        changeFragment(MapFragment)
                    }
                    R.id.menu_com -> {
                        changeFragment(ComFragment)
                    }
                    R.id.menu_mypage -> {
                        changeFragment(MypageFragment)
                    }
                }
                true
            }
            selectedItemId = R.id.menu_home
        }
    }


        private fun changeFragment(fragment: Fragment){
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayoutContainer, fragment)
                .commit()
        }




    }

