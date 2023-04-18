package com.mikirinkode.firebasechatapp.data.model

data class UserAccount(
    var userId: String? = "",
    var email: String? = "",
    var name: String? = "",
    var avatarUrl: String? = "",
    var createdAt: String? = "",
    var lastLoginAt: String? = "",
    var updatedAt: String? = "",
)