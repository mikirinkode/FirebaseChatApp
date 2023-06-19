package com.mikirinkode.firebasechatapp.firebase.user

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface FirebaseUserListListener {
    fun onGetAllUserDataSuccess(users: List<UserAccount>)
}
