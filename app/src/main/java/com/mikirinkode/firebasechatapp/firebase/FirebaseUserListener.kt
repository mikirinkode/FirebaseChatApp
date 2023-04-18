package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface FirebaseUserListener {
    fun onGetAllUserDataSuccess(users: List<UserAccount>)
}
