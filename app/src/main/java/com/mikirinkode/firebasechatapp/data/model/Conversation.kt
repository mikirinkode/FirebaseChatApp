package com.mikirinkode.firebasechatapp.data.model

data class Conversation(
    var conversationId: String? = "",
    var userIdList: List<String> = listOf(),
    var messages: Map<String, ChatMessage> = mapOf(), // TODO: better gini atau enggak

    var unreadMessages: Int = 0, // TODO: ini mending hitung di lokal atau get dari db aja
    var interlocutor: UserAccount? = null,
)
