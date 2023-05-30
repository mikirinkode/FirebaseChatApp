package com.mikirinkode.firebasechatapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

class FirebaseUserOnlineStatusHelper(
    private val mListener: UserOnlineStatusEventListener
) {
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
    private val database = FirebaseProvider.instance().getDatabase()
    private val usersRef = database?.getReference("users")

    fun getUserOnlineStatus(userId: String) {
        usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val statusModel = snapshot.getValue(UserRTDB::class.java)
                if (statusModel?.userId == userId) {
                    mListener.onUserOnlineStatusReceived(statusModel)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }

        })
        usersRef?.child(userId)?.get()?.addOnSuccessListener { snapshot ->
            val statusModel = snapshot.getValue(UserRTDB::class.java)
            if (statusModel?.userId == userId) {
                mListener.onUserOnlineStatusReceived(statusModel)
            }
        }
    }
}