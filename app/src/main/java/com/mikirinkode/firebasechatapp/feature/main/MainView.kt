package com.mikirinkode.firebasechatapp.feature.main

import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.Conversation

interface MainView: BaseView {
    fun onChatHistoryReceived(conversations: List<Conversation>)
}