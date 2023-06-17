package com.mikirinkode.firebasechatapp.feature.profile

import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

// TODO: check
class ProfileHelper(
    private val mListener: ProfileEventListener
) {

    private val firestore = FirebaseProvider.instance().getFirestore()

    fun getUserById(userId: String) {
        firestore?.collection("users")
            ?.whereEqualTo("userId", userId)
            ?.get()
            ?.addOnSuccessListener {
                val document = it.documents.first()
                val user: UserAccount? = document.toObject()
                if (user != null) {
                    mListener.onGetProfileSuccess(user)
                }
            }
    }
}

interface ProfileEventListener{
    fun onGetProfileSuccess(user: UserAccount)
}