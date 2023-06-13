package com.mikirinkode.firebasechatapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper

/**
 * common task that don't need listener
 * example: update user online status
 */
class CommonFirebaseTaskHelper {
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
    private val database = FirebaseProvider.instance().getDatabase()
    private val messaging = FirebaseProvider.instance().getMessaging()
    private val usersRef = database?.getReference("users")

    fun updateTypingStatus(isTyping: Boolean, currentReceiver: String){
        val userId = auth?.currentUser?.uid
        val newUpdate = hashMapOf<String, Any>(
            "typing" to isTyping,
            "currentlyTypingFor" to currentReceiver
        )

        if (userId != null) {
            usersRef?.child(userId)?.updateChildren(newUpdate)
        }
    }

    fun updateOnlineStatus() {
        val userId = auth?.currentUser?.uid
        val ref = database?.getReference(".info/connected")
        ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isConnected = snapshot.value

                // set to db
                if (userId != null) {
                    if (isConnected == true) {
                        val timestamp = System.currentTimeMillis()
                        val newUpdate = hashMapOf<String, Any>(
                            "userId" to userId, // TODO
                            "online" to true,
                            "lastOnlineTimestamp" to timestamp
                        )
                        usersRef?.child(userId)?.updateChildren(newUpdate)
                    } else {

                        val timestamp = System.currentTimeMillis()
                        val newUpdate = hashMapOf<String, Any>(
                            "userId" to userId,
                            "online" to false,
                            "lastOnlineTimestamp" to timestamp
                        )
                        usersRef?.child(userId)?.onDisconnect()?.updateChildren(newUpdate)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }

        })
    }

    fun observeToken(){
        val currentUserId = auth?.currentUser?.uid
        val userRef = database?.getReference("users")

        messaging?.token?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                val token = task.result

                // Save token to server if use a server
                if (currentUserId != null){
                    val currentDate = DateHelper.getCurrentDateTime()
                    userRef?.child(currentUserId)?.child("fcmToken")?.setValue(token)
                    userRef?.child(currentUserId)?.child("fcmTokenUpdatedAt")?.setValue(currentDate)
                }
            }
        }
    }
}