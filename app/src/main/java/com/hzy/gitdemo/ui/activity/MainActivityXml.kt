package com.hzy.gitdemo.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hzy.gitdemo.R
import com.hzy.gitdemo.ui.fragment.PopularFragment
import com.hzy.gitdemo.ui.fragment.SearchFragment
import com.hzy.gitdemo.ui.fragment.UserProfileFragment

class MainActivityXml : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_xml)
        
        setupBottomNavigation()
        
        // 默认显示热门页面
        if (savedInstanceState == null) {
            showFragment(PopularFragment())
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_popular -> {
                    showFragment(PopularFragment())
                    true
                }
                R.id.nav_search -> {
                    showFragment(SearchFragment())
                    true
                }
                R.id.nav_profile -> {
                    showFragment(UserProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}