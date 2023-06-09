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

    var isSelected: Boolean = false
): Parcelable