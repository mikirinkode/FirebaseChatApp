package com.mikirinkode.firebasechatapp.firebase.user

import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class FirebaseUserHelper(
    private val mListener: FirebaseUserListener
) {
    private val firestore = FirebaseProvider.instance().getFirestore()

    fun getUserById(userId: String) {
        val querySnapshot = firestore?.collection("users")
            ?.document(userId)
            ?.get()
            ?.addOnSuccessListener { document ->
                val user: UserAccount? = document?.toObject()
                if (user != null) {
                    mListener.onGetUserSuccess(user)
                }
            }
            ?.addOnFailureListener {

            }

    }
}