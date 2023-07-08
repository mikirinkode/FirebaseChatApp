package com.mikirinkode.firebasechatapp.feature.chat.chatroom.personal

import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserStatus

interface PersonalConversationListener {
    fun onInterlocutorDataReceived(user: UserAccount)
    fun onUserStatusReceived(status: UserStatus)
    fun onMessagesReceived(messages: List<ChatMessage>)
    fun showUploadImageProgress(progress: Int)
}