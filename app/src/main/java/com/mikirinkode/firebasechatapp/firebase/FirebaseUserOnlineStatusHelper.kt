package com.mikirinkode.firebasechatapp.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

class FirebaseUserOnlineStatusHelper(
    private val mListener: UserOnlineStatusEventListener
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val usersRef = database?.getReference("users")

    fun getUserOnlineStatus(userId: String) {
        usersRef?.child(userId)?.get()?.addOnSuccessListener { snapshot ->
            val statusModel = snapshot.getValue(UserRTDB::class.java)
            if (statusModel?.userId == userId) {
                mListener.onUserOnlineStatusReceived(statusModel)
            }
        }
//        usersRef?.addValueEventListener(object : ValueEventListener{
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (snapshot in dataSnapshot.children){
//                    val statusModel = snapshot.getValue(UserRTDB::class.java)
//                    if (statusModel?.userId == userId){
//                        mListener.onUserOnlineStatusReceived(statusModel)
//                        return
//                    }
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
////                TODO("Not yet implemented")
//            }
//
//        })
    }
}