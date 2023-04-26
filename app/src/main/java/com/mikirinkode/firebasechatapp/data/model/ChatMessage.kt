package com.mikirinkode.firebasechatapp.data.model

data class ChatMessage(
    val messageId: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val senderId: String = "",
    val receiverId: String = "",
    val deliveredTimestamp: Long = 0L,
    val readTimestamp: Long = 0L,
    val beenRead: Boolean = false
)
