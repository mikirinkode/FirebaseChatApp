package com.mikirinkode.firebasechatapp.data.model

data class OnlineStatus(
    val online: Boolean = false,
    val lastOnlineTimestamp: Long = 0L,
)