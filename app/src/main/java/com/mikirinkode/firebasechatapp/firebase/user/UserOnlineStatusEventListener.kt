package com.mikirinkode.firebasechatapp.firebase.user

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface UserOnlineStatusEventListener {
    fun onUserOnlineStatusReceived(status: UserAccount)
}
