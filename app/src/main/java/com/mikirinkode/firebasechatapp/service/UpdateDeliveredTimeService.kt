package com.mikirinkode.firebasechatapp.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlinx.coroutines.*

class UpdateDeliveredTimeService : Service() {

    companion object {
        private const val CHANNEL_ID = "ChitChatChannel"
        private const val CHANNEL_NAME = "MyChannel" // TODO: Create Constants
        private const val NOTIFICATION_ID = 123
        internal val TAG = UpdateDeliveredTimeService::class.java.simpleName
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val database = FirebaseProvider.instance().getDatabase()
    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val pref = LocalSharedPref.instance()

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    // TODO: check again later
    // sometimes error NPE on intent parameter
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val loggedUserId = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)?.userId
        val conversationIdList: List<String>? =
            pref?.getObjectsList(PreferenceConstant.CONVERSATION_ID_LIST, String::class.java)

        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        Log.d(TAG, "Service dijalankan...")
        serviceScope.launch {
            Log.d(TAG, "conversation id list size: ${conversationIdList?.size}")

            conversationIdList?.forEach { conversationId ->

                messagesRef?.child(conversationId)
                    ?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val theLatestMessage =
                                dataSnapshot.children.lastOrNull()
                                    ?.getValue(ChatMessage::class.java)

                            for (snapshot in dataSnapshot.children) {
                                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                                if (chatMessage != null) {
                                    if (chatMessage.senderId != loggedUserId) {
                                        if (chatMessage.deliveredTimestamp == 0L) {

                                            val timeStamp = System.currentTimeMillis()
                                            updateMessageDeliveredTime(
                                                conversationId,
                                                timeStamp,
                                                snapshot?.key ?: ""
                                            )

                                            if (chatMessage.messageId == theLatestMessage?.messageId) {
                                                updateLastMessageDeliveredTime(
                                                    conversationId,
                                                    timeStamp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            stopSelf()
                            Log.d(TAG, "Service dihentikan")
                            Log.d(TAG, "An Error Occured: ${error.message}")
                        }
                    })
            }

        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "onDestroy: Service dihentikan")
    }


    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingFlags: Int = if (Build.VERSION.SDK_INT >= 23) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingFlags)

        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Message Service")
            .setContentText("Checking for new message")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_NAME
            notificationBuilder.setChannelId(CHANNEL_ID)
            mNotificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

    private fun updateMessageDeliveredTime(
        conversationId: String,
        timeStamp: Long,
        messageId: String
    ) {
        val messageRef =
            messagesRef?.child(conversationId)?.child(messageId)?.ref
        messageRef?.child("deliveredTimestamp")?.setValue(timeStamp)
    }

    private fun updateLastMessageDeliveredTime(conversationId: String, timeStamp: Long) {
        val conversationRef = conversationsRef?.child(conversationId)?.child("lastMessage")?.ref
        val update = mapOf("deliveredTimestamp" to timeStamp)
        conversationRef?.updateChildren(update)
    }

}