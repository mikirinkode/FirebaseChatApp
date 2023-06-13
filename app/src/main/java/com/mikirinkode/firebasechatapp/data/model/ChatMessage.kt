package com.mikirinkode.firebasechatapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatMessage(
    val messageId: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val sendTimestamp: Long = 0L,
    val type: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val deliveredTimestamp: Long = 0L,
    val beenReadBy: Map<String, Long> = mapOf(),
//    val readTimestamp: Long = 0L,
//    val beenRead: Boolean = false,

    var isTheFirstUnreadMessage: Boolean = false,
    var isSelected: Boolean = false
): Parcelable
