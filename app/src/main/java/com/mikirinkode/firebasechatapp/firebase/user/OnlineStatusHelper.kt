package com.mikirinkode.firebasechatapp.firebase.user

import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class OnlineStatusHelper(
    private val mListener: OnlineStatusListener
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