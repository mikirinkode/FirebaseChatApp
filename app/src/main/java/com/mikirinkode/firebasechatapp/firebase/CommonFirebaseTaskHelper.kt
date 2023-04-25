package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus

/**
 * common task that don't need listener
 * example: update user online status
 */
class CommonFirebaseTaskHelper {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val userOnlineStatusRef = database?.getReference("userOnlineStatus")

    private val currentUser = auth?.currentUser

    fun updateUserOnlineStatus(){
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null){
                val userId = user.uid
                val timestamp = System.currentTimeMillis()

                val model = UserOnlineStatus(
                    userId = userId,
                    online = true,
                    lastOnlineTimestamp = timestamp
                )

                userOnlineStatusRef?.child(userId)?.setValue(model)
            } else {
                // User is signed out
                val userId = currentUser?.uid
                if (userId != null) {

                    // Update the user's online status
                    userOnlineStatusRef?.child(userId)?.child("online")?.setValue(false)
                }
            }
        }
    }
}