package com.mikirinkode.firebasechatapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val messageId: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0L,
    val type: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val deliveredTimestamp: Long = 0L,
    val readTimestamp: Long = 0L,
    val beenRead: Boolean = false
): Parcelable
