package com.mikirinkode.firebasechatapp

import android.app.Application
import android.content.Context
import android.content.Intent
import com.mikirinkode.firebasechatapp.constants.Constants
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.feature.chat.chatroom.ConversationActivity
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        mContext = this

        // Firebase Initialization
        FirebaseProvider.instance().initialize(this)

        // Shared Preferences Initialization
        LocalSharedPref.init(applicationContext)

        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(Constants.ONE_SIGNAL_APP_ID)
        OneSignal.setNotificationOpenedHandler { result: OSNotificationOpenedResult? ->
            val payload = result?.notification?.additionalData
            val conversationId = payload?.optString("conversationId")
            val conversationType = payload?.optString("conversationType")

            // Handle the chat ID
            if (conversationId != null && !conversationId.isEmpty()) {
                // Start the chat room activity and pass the chat ID
                val openIntent = Intent(this, ConversationActivity::class.java)
                openIntent.putExtra(
                    ConversationActivity.EXTRA_INTENT_CONVERSATION_ID,
                    conversationId
                )
                openIntent.putExtra(
                    ConversationActivity.EXTRA_INTENT_CONVERSATION_TYPE,
                    conversationType
                )
                openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(openIntent)
            }
        }
//        OneSignal.setNotificationWillShowInForegroundHandler(NotificationHandler())
    }

    companion object {
        lateinit var mContext: Context
    }
}