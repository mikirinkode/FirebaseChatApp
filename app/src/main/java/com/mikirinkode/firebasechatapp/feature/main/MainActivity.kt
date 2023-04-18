package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
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

    private val user: UserAccount? by lazy {
        pref?.getObject(DataConstant.USER, UserAccount::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        onActionClick()
    }

    private fun initView() {
        Log.e("Main", "user: $user")
        Log.e("Main", "user: ${user?.avatarUrl}")
        if (user != null) {
            if(user!!.avatarUrl != null && user!!.avatarUrl != ""){
                Glide.with(this)
                    .load(user!!.avatarUrl).into(binding.ivUserAvatar)
            }
        }

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