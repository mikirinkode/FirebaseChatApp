package com.mikirinkode.firebasechatapp.feature.chat

import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus

interface ChatView: BaseView {
    fun updateMessages(messages: List<ChatMessage>)

    fun updateReceiverOnlineStatus(status: UserOnlineStatus)
}