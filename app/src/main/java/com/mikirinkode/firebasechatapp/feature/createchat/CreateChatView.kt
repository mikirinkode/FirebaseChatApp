package com.mikirinkode.firebasechatapp.feature.createchat

import android.net.Uri
import com.mikirinkode.firebasechatapp.base.view.BaseView

interface CreateChatView: BaseView {
    fun onImageCaptured(capturedImage: Uri?)
    fun onSuccessCreateGroupChat(conversationId: String)
}