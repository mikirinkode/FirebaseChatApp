package com.mikirinkode.firebasechatapp.feature.profile

import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider


class ProfileHelper(
    private val mListener: ProfileEventListener
) {

    private val pref = LocalSharedPref.instance()
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
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

    fun logout(){
        val userId = auth?.currentUser?.uid
        if (userId != null) {
            val userRef = firestore?.collection("users")?.document(userId)

            val timestamp = System.currentTimeMillis()
            val newUpdate = hashMapOf<String, Any>(
                "online" to false,
                "lastOnlineTimestamp" to timestamp,
                "oneSignalToken" to "",
                "oneSignalTokenUpdatedAt" to timestamp
            )

            userRef?.set(newUpdate, SetOptions.merge())
            pref?.clearSession()
            auth?.signOut()
            mListener.onLogoutSuccess()
        }
    }
}

interface ProfileEventListener{
    fun onGetProfileSuccess(user: UserAccount)

    fun onLogoutSuccess()
}