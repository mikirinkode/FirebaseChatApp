package com.mikirinkode.firebasechatapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.SetOptions
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper
import com.onesignal.OneSignal

/**
 * common task that don't need listener
 * example: update user online status
 */
class CommonFirebaseTaskHelper {
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
    private val database = FirebaseProvider.instance().getDatabase()
    private val messaging = FirebaseProvider.instance().getMessaging()
    private val fireStore = FirebaseProvider.instance().getFirestore()
    private val conversationsRef = database?.getReference("conversations")

    fun updateOneSignalDeviceToken() {
        // Retrieve the device token
        val deviceState = OneSignal.getDeviceState()
        val deviceToken = deviceState?.userId

        // Check if device token is available
        if (deviceToken != null) {
            val currentUserId = auth?.currentUser?.uid
            val userRef = currentUserId?.let { fireStore?.collection("users")?.document(it) }

            // Save token to server if use a server
            if (currentUserId != null) {
                val currentDate = DateHelper.getCurrentDateTime()
                val newUpdate = hashMapOf<String, Any>(
                    "oneSignalToken" to deviceToken,
                    "oneSignalTokenUpdatedAt" to currentDate
                )

                userRef?.set(newUpdate, SetOptions.merge())
            }
        }
    }


    fun updateTypingStatus(isTyping: Boolean, conversationId: String) {
        val userId = auth?.currentUser?.uid
        if (userId != null) {
            conversationsRef?.child(conversationId)?.child("typingUser")?.child(userId)?.setValue(isTyping)
        }
    }

//    fun updateTypingStatus(isTyping: Boolean, currentReceiver: String) {
//        val userId = auth?.currentUser?.uid
//
//        val userRef = userId?.let { fireStore?.collection("users")?.document(it) }
//        val newUpdate = hashMapOf<String, Any>(
//            "typing" to isTyping,
//            "currentlyTypingFor" to currentReceiver
//        )
//
//        if (userId != null) {
//            userRef?.set(newUpdate, SetOptions.merge())
//        }
//    }

    fun updateOnlineStatus() {
        val userId = auth?.currentUser?.uid
        val userRef = userId?.let { fireStore?.collection("users")?.document(it) }

        val ref = database?.getReference(".info/connected")
        ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.value

                // set to db
                if (userId != null) {
                    if (isConnected == true) {
                        val timestamp = System.currentTimeMillis()
                        val newUpdate = hashMapOf<String, Any>(
                            "online" to true,
                            "lastOnlineTimestamp" to timestamp
                        )
                        userRef?.set(newUpdate, SetOptions.merge())
                    } else {

                        val timestamp = System.currentTimeMillis()
                        val newUpdate = hashMapOf<String, Any>(
                            "online" to false,
                            "lastOnlineTimestamp" to timestamp
                        )
                        userRef?.set(newUpdate, SetOptions.merge())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }

        })
    }
}