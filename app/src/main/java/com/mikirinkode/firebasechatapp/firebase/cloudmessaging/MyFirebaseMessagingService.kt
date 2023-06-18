package com.mikirinkode.firebasechatapp.firebase.cloudmessaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.feature.chat.ConversationActivity
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "ChitChatChannel"
        private const val CHANNEL_NAME = "MyChannel"
        private const val notificationId = 123
    }


    // TODO: update
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val conversationId = remoteMessage.data["conversationId"] // TODO
        val senderId = remoteMessage.data["senderId"]

        val title = remoteMessage.notification?.title ?: ""
        val messageText = remoteMessage.notification?.body ?: ""

        val intent = Intent(this, ConversationActivity::class.java)
            .putExtra(ConversationActivity.EXTRA_INTENT_CONVERSATION_ID, conversationId)
            .putExtra(ConversationActivity.EXTRA_INTENT_INTERLOCUTOR_ID, senderId)

        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addParentStack(ConversationActivity::class.java)
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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val firebaseFirestore = FirebaseProvider.instance().getFirestore()
        val auth = FirebaseProvider.instance().getFirebaseAuth()
        val currentUserId = auth?.currentUser?.uid
        val userRef = currentUserId?.let { firebaseFirestore?.collection("users")?.document(it) }

        if (currentUserId != null) {
            val currentDate = DateHelper.getCurrentDateTime()
            val updates = hashMapOf<String, Any>(
                "fcmToken" to token,
                "fcmTokenUpdatedAt" to currentDate
            )
            userRef?.set(updates, SetOptions.merge())
        }
    }
}