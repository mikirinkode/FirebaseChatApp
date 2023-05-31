package com.mikirinkode.firebasechatapp.firebase.cloudmessaging

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.feature.chat.ChatActivity
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.mikirinkode.firebasechatapp.helper.DateHelper
import com.mikirinkode.firebasechatapp.service.UpdateDeliveredTimeService
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "ChitChatChannel"
        private const val CHANNEL_NAME = "MyChannel"
        private const val notificationId = 123
    }


    // TODO: update delivered time and on click message
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("MessagingServiece", "onMessageReceived")
        val messageId = remoteMessage.data["messageId"].toString()
        val conversationId = remoteMessage.data["conversationId"].toString()
        val receiverId = remoteMessage.data["receiverId"]
        val senderId = remoteMessage.data["senderId"]


        val title = remoteMessage.notification?.title ?: ""
        val messageText = remoteMessage.notification?.body ?: ""
        val clickAction = remoteMessage.notification?.clickAction

        // TODO: update delivered time and on click message
//        val serviceIntent = Intent(this, UpdateDeliveredTimeService::class.java)
//        serviceIntent.putExtra("message", messageText)
//        startService(serviceIntent)

        val intent = Intent(this, ChatActivity::class.java)
            .putExtra(ChatActivity.EXTRA_INTENT_INTERLOCUTOR_ID, senderId)

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addParentStack(ChatActivity::class.java)
            addNextIntent(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(110, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(110, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        // Need to create notification channel for android Oreo and above
        createNotificationChannel()

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(messageText)
            .setChannelId(CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // show the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        super.onMessageReceived(remoteMessage)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = CHANNEL_NAME

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateMessageDeliveredTime(
        message: String,
        conversationId: String,
        messageId: String,
        timestamp: Long
    ) {
        val database = FirebaseProvider.instance().getDatabase()

        val conversationsRef = database?.getReference("conversations")

//        val messageRef =
//            conversationsRef?.child(conversationId)?.child("messages")?.child(messageId)?.ref
//        messageRef?.child("deliveredTimestamp")?.setValue(timestamp)

        val newData = database?.getReference("testing")?.push()

        val value = mapOf(
            "message" to message,
            "deliveredTimestamp" to timestamp
        )
        newData?.setValue(value)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val database = FirebaseProvider.instance().getDatabase()
        val userRef = database?.getReference("users")

        val auth = FirebaseProvider.instance().getFirebaseAuth()
        val currentUserId = auth?.currentUser?.uid

        if (currentUserId != null) {
            val currentDate = DateHelper.getCurrentDateTime()
            userRef?.child(currentUserId)?.child("fcmToken")?.setValue(token)
            userRef?.child(currentUserId)?.child("fcmTokenUpdatedAt")?.setValue(currentDate)
        }
    }
}