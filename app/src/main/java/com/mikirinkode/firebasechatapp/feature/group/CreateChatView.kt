package com.mikirinkode.firebasechatapp.feature.group

import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BaseView

interface CreateChatView: BaseView {
    fun onImageCaptured(capturedImage: Uri?)
    fun onSuccessCreateGroupChat(conversationId: String)
}