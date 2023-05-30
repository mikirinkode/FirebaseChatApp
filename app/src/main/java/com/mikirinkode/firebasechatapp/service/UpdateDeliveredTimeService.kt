package com.mikirinkode.firebasechatapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.mikirinkode.firebasechatapp.helper.DateHelper

// TODO: Still not working
class UpdateDeliveredTimeService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        return super.onStartCommand(intent, flags, startId)
        if (intent != null){
            val message = intent.getStringExtra("message")

            // update delivered time
            if (message != null){
                val data = mapOf(
                    "fromService" to true,
                    "message" to message,
                    "timestamp" to DateHelper.getCurrentDateTime()
                )

                val database = FirebaseProvider.instance().getDatabase()
                val ref = database?.reference?.child("notifications")
                val notificationId = ref?.push()?.key ?: ""
                ref?.child(notificationId)?.setValue(data)
            }
        }
        // Stop the service once the task is completed
        stopSelf()

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}