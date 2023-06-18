package com.mikirinkode.firebasechatapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserAccount(
    var userId: String? = "",
    var email: String? = "",
    var name: String? = "",
    var avatarUrl: String? = "",
    var createdAt: String? = "",
    var lastLoginAt: String? = "",
    var updatedAt: String? = "",

    val online: Boolean = false,
    val typing: Boolean = false,
    val currentlyTypingFor: String = "",
    val lastOnlineTimestamp: Long = 0L,
    val conversationIdList: List<String> = listOf<String>(),

    var isSelected: Boolean = false
): Parcelable