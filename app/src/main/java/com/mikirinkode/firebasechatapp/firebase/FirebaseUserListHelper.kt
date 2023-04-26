package com.mikirinkode.firebasechatapp.firebase

import android.util.Log
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.UserAccount

class FirebaseUserListHelper(
    val mListener: FirebaseUserListener
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val firestore = FirebaseHelper.instance().getFirestore()


    fun getUserList() {
        val currentUser = auth?.currentUser
        Log.e(TAG, "getUserList")
        val userList = ArrayList<UserAccount>()

        firestore?.collection("users")
            ?.whereNotEqualTo("userId", currentUser?.uid)
            ?.get()
            ?.addOnSuccessListener { documentList ->
                Log.e(TAG, "getUserList addOnSuccessListener")
                for (document in documentList) {
                    if (document != null) {
                        val userAccount: UserAccount = document.toObject()
                        userList.add(userAccount)
                    }
                    Log.e(TAG, "getUserList ${userList.size}")
                }
                Log.e(TAG, "getUserList ${userList.size}")
                mListener.onGetAllUserDataSuccess(userList)
            }
            ?.addOnFailureListener {
                Log.e(TAG, "getUserList addOnFailureListener")
                // TODO: on fail
            }
    }

    companion object {
        private const val TAG = "UserListHelper"
    }
}