package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding
import com.mikirinkode.firebasechatapp.feature.login.LoginActivity
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity
import com.mikirinkode.firebasechatapp.feature.userlist.UserListActivity
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class MainActivity : AppCompatActivity(), MainView {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val user: UserAccount? by lazy {
        pref?.getObject(DataConstant.USER, UserAccount::class.java)
    }

    private lateinit var presenter: MainPresenter

    private val chatHistoryAdapter: ChatHistoryAdapter by lazy {
        ChatHistoryAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkPermission()
        setupPresenter()
        initView()
        onActionClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun onResume() {
        super.onResume()
//        if (presenter != null) {
//            presenter.updateUserOnlineStatus()
//            presenter.getMessageHistory()
//        } else {
//            setupPresenter()
//        }
    }

    private fun initView() {
        binding.apply {
            if (user != null) {
                if (user!!.avatarUrl != null && user!!.avatarUrl != "") {
                    Glide.with(this@MainActivity)
                        .load(user!!.avatarUrl).into(binding.ivUserAvatar)
                }

                rvChatHistory.layoutManager = LinearLayoutManager(this@MainActivity)
                rvChatHistory.adapter = chatHistoryAdapter
                chatHistoryAdapter.setLoggedUserId(user?.userId.toString())

                presenter.getMessageHistory()
            }
        }
    }

    private fun setupPresenter() {
        presenter = MainPresenter()
        presenter.attachView(this)
        presenter.updateUserOnlineStatus()
    }

    private fun checkPermission() {
        if (!PermissionManager.isNotificationPermissionGranted(this)) {
            Toast.makeText(this, "Notification Permission Not Granted", Toast.LENGTH_SHORT).show()
            PermissionManager.requestNotificationPermission(this)
        } else {
            Toast.makeText(this, "Notification Permission Already Granted", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onChatHistoryReceived(conversations: List<Conversation>) {
        if (conversations.isNotEmpty()){
            chatHistoryAdapter.setData(conversations)
        }
    }

    override fun showLoading() {}
    override fun hideLoading() {}

    private fun onActionClick() {
        binding.apply {
            ivUserAvatar.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, ProfileActivity::class.java).putExtra(
                        ProfileActivity.EXTRA_INTENT_USER_ID,
                        user?.userId
                    )
                )
            }

            btnNewChat.setOnClickListener {
                startActivity(Intent(this@MainActivity, UserListActivity::class.java))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.NOTIFICATION_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}