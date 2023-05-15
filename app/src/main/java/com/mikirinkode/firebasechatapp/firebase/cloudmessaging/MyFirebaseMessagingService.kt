package com.mikirinkode.firebasechatapp.firebase.cloudmessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mikirinkode.firebasechatapp.R

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.e("MessagingServiece", "onMessageReceived")

        val title = message.data["title"]
        val messageText = message.data["message"]
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ChitChatChannel"

        Log.e("MessagingServiece", "title: $title")
        Log.e("MessagingServiece", "message: $messageText")
        Log.e("MessagingServiece", "remotemessage: ${message.notification?.title}") // kalo dari campaign pake ini datanya
        Log.e("MessagingServiece", "remotemessage: ${message.notification?.body}")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Channel name", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())

        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
//        sendRegistrationToServer(token)
    }
}