package com.hzy.gitdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hzy.gitdemo.ui.activity.MainActivityXml

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 直接进入主页面，无需登录检查
        val intent = Intent(this@MainActivity, MainActivityXml::class.java)
        startActivity(intent)
        finish()
    }
}