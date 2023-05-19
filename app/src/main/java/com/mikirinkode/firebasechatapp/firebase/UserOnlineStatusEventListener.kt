package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserRTDB

interface UserOnlineStatusEventListener {
    fun onUserOnlineStatusReceived(status: UserRTDB)
}
