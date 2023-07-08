package com.mikirinkode.firebasechatapp.feature.chat.chatroom.personal

import android.net.Uri
import com.mikirinkode.firebasechatapp.data.model.*

interface PersonalConversationView {
    fun onInterlocutorDataReceived(user: UserAccount)
    fun onUserStatusReceived(status: UserStatus)

    fun onMessagesReceived(messages: List<ChatMessage>)



    fun onImageCaptured(capturedImage: Uri?)

    fun showOnUploadImageProgress(progress: Int)
}