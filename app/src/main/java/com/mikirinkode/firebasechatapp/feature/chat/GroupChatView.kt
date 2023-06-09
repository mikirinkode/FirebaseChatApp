package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB

interface GroupChatView {
    fun onReceiveGroupData(conversation: Conversation)

    fun onMessagesReceived(messages: List<ChatMessage>)

    fun onImageCaptured(capturedImage: Uri?)

    fun showOnUploadImageProgress(progress: Int)
}
