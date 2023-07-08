package com.mikirinkode.firebasechatapp.feature.chat.chatroom.personal

import android.net.Uri
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.OnlineStatus
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface PersonalConversationView {
    fun onMessagesReceived(messages: List<ChatMessage>)

    fun onInterlocutorDataReceived(user: UserAccount)


    fun onImageCaptured(capturedImage: Uri?)

    fun showOnUploadImageProgress(progress: Int)
}