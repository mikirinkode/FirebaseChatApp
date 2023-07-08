package com.mikirinkode.firebasechatapp.data.model

data class TypingStatus(
    val typing: Boolean = false,
    val typingFor: String? = null,
)
