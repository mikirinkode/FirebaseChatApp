package com.mikirinkode.firebasechatapp.feature.userlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mikirinkode.firebasechatapp.databinding.ActivityUserListBinding

class UserListActivity : AppCompatActivity() {

    private val binding: ActivityUserListBinding by lazy {
        ActivityUserListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        onActionClicked()
    }

    private fun onActionClicked() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}