package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface FirebaseUserListListener {
    fun onGetAllUserDataSuccess(users: List<UserAccount>)
}
