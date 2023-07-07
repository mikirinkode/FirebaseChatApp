package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding
import com.mikirinkode.firebasechatapp.feature.createchat.CreateNewChatActivity
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity
import com.mikirinkode.firebasechatapp.service.UpdateDeliveredTimeService
import com.mikirinkode.firebasechatapp.commonhelper.PermissionHelper

class MainActivity : AppCompatActivity(), MainView {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val user: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }

    private lateinit var presenter: MainPresenter

    private val conversationListAdapter: ConversationListAdapter by lazy {
        ConversationListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkPermission()
        setupPresenter()
        initView()
        onActionClick()

        runService()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
    override fun onResume() {
        super.onResume()
        if (presenter != null) {
            presenter.updateUserOnlineStatus()
            presenter.getMessageHistory()
        } else {
            setupPresenter()
        }
    }

    private fun runService() { // TODO

        // Create a repeating request for the worker
//        val repeatingRequest = PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES)
//            .build()

        // Enqueue the repeating request with WorkManager
//        WorkManager.getInstance(this).enqueue(repeatingRequest)

//        val serviceIntent = Intent(this, UpdateDeliveredTimeService::class.java)
//        if (Build.VERSION.SDK_INT >= 26) {
//            startForegroundService(serviceIntent)
//        } else {
//            startService(serviceIntent)
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
                rvChatHistory.adapter = conversationListAdapter
                conversationListAdapter.setLoggedUserId(user?.userId.toString())

                presenter.getMessageHistory()
            }
        }
    }

    private fun setupPresenter() {
        presenter = MainPresenter()
        presenter.attachView(this)
        presenter.updateUserOnlineStatus()
        presenter.updateOneSignalToken()
    }

    private fun checkPermission() {
        if (!PermissionHelper.isNotificationPermissionGranted(this)) {
            Toast.makeText(this, "Notification Permission Not Granted", Toast.LENGTH_SHORT).show()
            PermissionHelper.requestNotificationPermission(this)
        } else {
            Toast.makeText(this, "Notification Permission Already Granted", Toast.LENGTH_SHORT)
                .show()

        }
    }

    override fun onConversationListReceived(conversations: List<Conversation>) {
        if (conversations.isNotEmpty()) {
            conversationListAdapter.setData(conversations)
        }
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

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
                startActivity(
                    Intent(this@MainActivity, CreateNewChatActivity::class.java).putExtra(
                        CreateNewChatActivity.EXTRA_INTENT_CONVERSATION_TYPE,
                        ConversationType.PERSONAL.toString()
                    )
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.NOTIFICATION_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}