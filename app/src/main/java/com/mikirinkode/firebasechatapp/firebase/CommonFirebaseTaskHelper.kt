package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserRTDB

/**
 * common task that don't need listener
 * example: update user online status
 */
class CommonFirebaseTaskHelper {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val usersRef = database?.getReference("users")

    private val currentUser = auth?.currentUser

    // TODO: check the logic again later
    fun updateUserOnlineStatus(){
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null){
                val userId = user.uid
                val timestamp = System.currentTimeMillis()

                val model = UserRTDB(
                    userId = userId,
                    online = true,
                    lastOnlineTimestamp = timestamp
                )
                val newUpdate = hashMapOf<String, Any>(
                    "userId" to userId,
                    "online" to true,
                    "lastOnlineTimestamp" to timestamp
                )
//                usersRef?.child(userId)?.setValue(model)
                usersRef?.child(userId)?.updateChildren(newUpdate)
            } else {
                // User is signed out
                val userId = currentUser?.uid
                if (userId != null) {
                    // Update the user's online status
                    usersRef?.child(userId)?.child("online")?.setValue(false)
                }
            }
        }
    }
}