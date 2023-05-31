package com.mikirinkode.firebasechatapp.data.model

data class Conversation(
    var conversationId: String? = "",
    var participants: List<String> = listOf(),
    var lastMessage: ChatMessage? = null, // Kalau pakai gini waktu update delivered dan read status, dia perlu diupdate juga. karna object nya ada 2
    var unreadMessages: Int = 0, // TODO: ini mending hitung di lokal atau get dari db aja

    var interlocutor: UserAccount? = null
)
