package com.mikirinkode.firebasechatapp.data.model

data class Conversation(
    var conversationId: String? = "",
    var userIdList: List<String> = listOf(),
    var interlocutor: UserAccount? = null,
    var lastMessage: String? = "",
    var lastMessageTimestamp: Long = 0L,
    var lastSenderId: String? = "",
)
