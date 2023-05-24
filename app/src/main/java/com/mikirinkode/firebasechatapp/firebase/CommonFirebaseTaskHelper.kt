package com.mikirinkode.firebasechatapp.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

/**
 * common task that don't need listener
 * example: update user online status
 */
class CommonFirebaseTaskHelper {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val usersRef = database?.getReference("users")
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
                            "userId" to userId,
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
//                        usersRef?.child(userId)?.onDisconnect()?.updateChildren(mapOf( "online" to false ))
                        usersRef?.child(userId)?.onDisconnect()?.updateChildren(newUpdate)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }

        })
    }

}