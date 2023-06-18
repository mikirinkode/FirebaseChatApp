package com.mikirinkode.firebasechatapp.firebase

import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

interface UserOnlineStatusEventListener {
    fun onUserOnlineStatusReceived(status: UserAccount)
}
