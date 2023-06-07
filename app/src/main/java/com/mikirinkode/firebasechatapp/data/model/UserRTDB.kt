package com.mikirinkode.firebasechatapp.data.model

/**
 * Class that used to store data user from Firebase Realtime Database
 */
data class UserRTDB(
    val userId: String = "",
    val online: Boolean = false,
    val typing: Boolean = false,
    val currentlyTypingFor: String = "",
    val lastOnlineTimestamp: Long = 0L,
    val conversationIdList: Map<String, Any> = mapOf()
)
