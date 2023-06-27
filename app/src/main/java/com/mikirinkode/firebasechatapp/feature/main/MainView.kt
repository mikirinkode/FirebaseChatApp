package com.mikirinkode.firebasechatapp.feature.main

import com.mikirinkode.firebasechatapp.base.view.BaseView
import com.mikirinkode.firebasechatapp.data.model.Conversation

interface MainView: BaseView {
    fun onConversationListReceived(conversations: List<Conversation>)
}