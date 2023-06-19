package com.mikirinkode.firebasechatapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mikirinkode.firebasechatapp.feature.chat.ConversationActivity

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Handle the notification here
        // Start the desired activity
        val openIntent = Intent(context, ConversationActivity::class.java)
        openIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context?.startActivity(openIntent)
    }
}