package com.mikirinkode.firebasechatapp.data.model

data class Conversation(
    var conversationId: String? = "",
    var userIdList: List<String> = listOf(),
    var messages: Map<String, ChatMessage> = mapOf(),

    var unreadMessages: Int = 0,
    var interlocutor: UserAccount? = null,
)
