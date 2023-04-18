package com.mikirinkode.firebasechatapp.feature.chat

import com.mikirinkode.firebasechatapp.base.BaseView

interface ChatView: BaseView {
    fun updateMessages(messages: List<ChatMessage>)
}