package com.mikirinkode.firebasechatapp.data.model

/**
 * Class that used to store data user from Firebase Realtime Database
 */
data class UserRTDB(
    val userId: String = "",
    val online: Boolean = false,
    val lastOnlineTimestamp: Long = 0L,
    val fcmToken: String = "",
)
