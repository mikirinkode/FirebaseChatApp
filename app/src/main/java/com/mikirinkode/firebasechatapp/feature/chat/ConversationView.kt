package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

interface ConversationView: BaseView {
    fun onMessagesReceived(messages: List<ChatMessage>)

    fun updateReceiverOnlineStatus(status: UserAccount)

    fun onImageCaptured(capturedImage: Uri?)

    fun onGetInterlocutorProfileSuccess(user: UserAccount)

    fun showOnUploadImageProgress(progress: Int)
    fun onReceiveGroupData(conversation: Conversation)
}