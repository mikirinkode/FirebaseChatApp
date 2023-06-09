package com.mikirinkode.firebasechatapp.feature.group

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.ActivityCreateGroupChatBinding

class CreateGroupChatActivity : AppCompatActivity() {

    private val binding: ActivityCreateGroupChatBinding by lazy {
        ActivityCreateGroupChatBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = findNavController(R.id.navHostCreateGroupChat)
        navController.setGraph(R.navigation.create_chat_group_navigation)
    }
}