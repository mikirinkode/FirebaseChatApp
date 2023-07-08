package com.mikirinkode.firebasechatapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Conversation(
    var conversationId: String? = "",
    var participants: Map<String, ParticipantStatus> = mapOf(), // TODO: Update
    var lastMessage: ChatMessage? = null,
    var conversationType: String? = "",
    var conversationAvatar: String? = "",
    var conversationName: String? = "",
    var createdAt: Long? = 0,
    var createdBy: String? = "",
    var unreadMessageEachParticipant: Map<String, Int> = mapOf(),

    var interlocutor: UserAccount? = null
)

data class ParticipantStatus(
    var joinedAt: Long? = 0L,
    var typing: Boolean = false,
)
