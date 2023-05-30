package com.mikirinkode.firebasechatapp.firebase

import com.google.firebase.auth.FirebaseUser
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface FirebaseUserListener {
    fun onGetUserSuccess(user: UserAccount)
}