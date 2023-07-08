package com.mikirinkode.firebasechatapp.firebase.user

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserStatus
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class OnlineStatusHelper(
    private val mListener: OnlineStatusListener
) {
    private val fireStore = FirebaseProvider.instance().getFirestore()
    private val database = FirebaseProvider.instance().getDatabase()
    private val usersRef = database?.getReference("users")

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

    fun getUserStatus(userId: String){
        usersRef?.child(userId)?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(UserStatus::class.java)
                if (status != null) {
                    mListener?.onUserStatusReceived(status)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}