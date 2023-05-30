package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

interface ChatView: BaseView {
    fun onMessagesReceived(messages: List<ChatMessage>)

    fun updateReceiverOnlineStatus(status: UserRTDB)

    fun onImageCaptured(capturedImage: Uri?)

    fun onGetInterlocutorProfileSuccess(user: UserAccount)
}