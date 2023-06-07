package com.mikirinkode.firebasechatapp.firebase

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount

class FirebaseUserListHelper(
    val mListener: FirebaseUserListListener
) {
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
    private val firestore = FirebaseProvider.instance().getFirestore()


    fun getUserList() {
        val currentUser = auth?.currentUser
        val userList = ArrayList<UserAccount>()

        firestore?.collection("users")
            ?.whereNotEqualTo("userId", currentUser?.uid)
            ?.get()
            ?.addOnSuccessListener { documentList ->
                for (document in documentList) {
                    if (document != null) {
                        val userAccount: UserAccount = document.toObject()
                        userList.add(userAccount)
                    }
                }
                mListener.onGetAllUserDataSuccess(userList)
            }
            ?.addOnFailureListener {
                // TODO: on fail
            }
    }

    companion object {
        private const val TAG = "UserListHelper"
    }
}