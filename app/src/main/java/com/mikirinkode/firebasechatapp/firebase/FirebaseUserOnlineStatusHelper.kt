package com.mikirinkode.firebasechatapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus

class FirebaseUserOnlineStatusHelper(
    private val mListener: UserOnlineStatusEventListener
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val userOnlineStatusRef = database?.getReference("userOnlineStatus")

    fun getUserOnlineStatus(userId: String) {
        userOnlineStatusRef?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children){
                    val statusModel = snapshot.getValue(UserOnlineStatus::class.java)
                    if (statusModel?.userId == userId){
                        mListener.onUserOnlineStatusReceived(statusModel)
                        return
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }

        })
    }
}