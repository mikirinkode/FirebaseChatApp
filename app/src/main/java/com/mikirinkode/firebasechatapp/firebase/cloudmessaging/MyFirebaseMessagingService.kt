package com.mikirinkode.firebasechatapp.firebase.cloudmessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.feature.chat.ChatActivity
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.mikirinkode.firebasechatapp.helper.DateHelper
import com.mikirinkode.firebasechatapp.service.UpdateDeliveredTimeService

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

        val title = remoteMessage.notification?.title ?: ""
        val messageText = remoteMessage.notification?.body ?: ""
        val clickAction = remoteMessage.notification?.clickAction

        val serviceIntent = Intent(this, UpdateDeliveredTimeService::class.java)
        serviceIntent.putExtra("message", messageText)
        startService(serviceIntent)

        val intent =
            Intent(this, ChatActivity::class.java)
                .putExtra(
                    ChatActivity.EXTRA_INTENT_INTERLOCUTOR_ID,
                    receiverId
                )
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)

        createNotificationChannel(title, messageText)

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(messageText)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // show the notification
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        super.onMessageReceived(remoteMessage)
    }

    private fun createNotificationChannel(title: String, messageText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = CHANNEL_NAME
            val channelDescription = messageText
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)
            channel.description = channelDescription

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