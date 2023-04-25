package com.mikirinkode.firebasechatapp.data.model

data class ChatMessage(
    val message: String = "",
    val timestamp: Long = 0L,
    val senderId: String = "",
    val receiverId: String = "",
)
