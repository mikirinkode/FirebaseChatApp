package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding
import com.mikirinkode.firebasechatapp.feature.login.LoginActivity
import com.mikirinkode.firebasechatapp.feature.userlist.UserListActivity

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onActionClick()
    }

    private fun onActionClick() {
        binding.apply {
            btnNewChat.setOnClickListener{
                startActivity(Intent(this@MainActivity, UserListActivity::class.java))
            }

            btnLogout.setOnClickListener {
                pref?.clearSession()
                Firebase.auth.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finishAffinity()
            }
        }
    }
}