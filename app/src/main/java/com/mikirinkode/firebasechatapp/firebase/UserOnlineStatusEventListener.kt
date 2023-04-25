package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus

interface UserOnlineStatusEventListener {
    fun onUserOnlineStatusReceived(status: UserOnlineStatus)
}
