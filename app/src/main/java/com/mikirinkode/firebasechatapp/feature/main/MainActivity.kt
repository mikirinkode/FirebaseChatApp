package com.mikirinkode.firebasechatapp.feature.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.data.local.pref.DataConstant
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding
import com.mikirinkode.firebasechatapp.feature.chat.ChatActivity
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity
import com.mikirinkode.firebasechatapp.feature.userlist.UserListActivity
import com.mikirinkode.firebasechatapp.service.UpdateDeliveredTimeService
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

        runService()
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

    private fun runService() {
        val serviceIntent = Intent(this, UpdateDeliveredTimeService::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
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
            Toast.makeText(this, "Notification Permission Already Granted", Toast.LENGTH_SHORT)
                .show()

        }
    }

    override fun onChatHistoryReceived(conversations: List<Conversation>) {
        if (conversations.isNotEmpty()) {
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

            tvAppName.setOnClickListener { // TODO: Delete Later
                showDummyNotification()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_01"
        private const val CHANNEL_NAME = "dicoding channel"
    }

    private fun showDummyNotification() {
        val intent = Intent(this, ChatActivity::class.java)
            .putExtra(ChatActivity.EXTRA_INTENT_INTERLOCUTOR_ID, "2dIi2rwPCxMBZewgaYUdl5bj7mB3")

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addParentStack(ChatActivity::class.java)
            addNextIntent(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(
                    110,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                getPendingIntent(110, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dummy Title")
            .setContentText("Dummy Message")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /* Create or update. */
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_NAME

            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)

            notificationBuilder.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }


        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
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
                    Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}