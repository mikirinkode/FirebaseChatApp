package com.mikirinkode.firebasechatapp.firebase.user

import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserStatus

interface OnlineStatusListener {
    fun onUserOnlineStatusReceived(status: UserAccount)

    fun onUserStatusReceived(status: UserStatus)
}
