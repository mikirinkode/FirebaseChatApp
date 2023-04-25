package com.mikirinkode.firebasechatapp.data.model

data class UserOnlineStatus(
    val userId: String = "",
    val online: Boolean = false,
    val lastOnlineTimestamp: Long = 0L,
)
