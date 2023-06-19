package com.mikirinkode.firebasechatapp.notification

import android.content.Context
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal

class NotificationHandler: OneSignal.OSRemoteNotificationReceivedHandler {
    override fun remoteNotificationReceived(context: Context?, notification: OSNotificationReceivedEvent?) {
        val customData = notification?.notification?.additionalData

        // Handle the custom data
        if (customData != null) {
            // Access custom data values
            val conversationId = customData.optString("conversationId")
            val conversationType = customData.optString("conversationType")
            val interlocutorId = customData.optString("interlocutorId")

            // Handle the custom data values as needed

        }
    }
}