package com.mikirinkode.firebasechatapp.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FirebaseUserListHelper(
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()


    fun getUserList() {
        Log.e(TAG, "getUserList")
        val userList = ArrayList<FirebaseUser>()


    }

    companion object {
        private const val TAG = "UserListHelper"
    }
}