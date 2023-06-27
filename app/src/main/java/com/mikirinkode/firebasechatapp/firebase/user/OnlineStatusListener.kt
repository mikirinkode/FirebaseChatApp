package com.mikirinkode.firebasechatapp.firebase.user

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface OnlineStatusListener {
    fun onUserOnlineStatusReceived(status: UserAccount)
}
