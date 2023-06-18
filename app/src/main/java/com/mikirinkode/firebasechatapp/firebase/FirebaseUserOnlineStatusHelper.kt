package com.mikirinkode.firebasechatapp.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

class FirebaseUserOnlineStatusHelper(
    private val mListener: UserOnlineStatusEventListener
) {
    private val fireStore = FirebaseProvider.instance().getFirestore()


    fun getUserOnlineStatus(userId: String) {
        val userRef = fireStore?.collection("users")?.document(userId)

        userRef?.addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                val user: UserAccount? = snapshot.toObject()
                if (user != null) {
                    mListener.onUserOnlineStatusReceived(user)
                }
            }
        }

    }
}