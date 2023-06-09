package com.mikirinkode.firebasechatapp.feature.group

import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface CreateGroupChatView: BaseView {
    fun setDataToRecyclerView(users: List<UserAccount>)
    fun onImageCaptured(capturedImage: Uri?)
    fun onSuccessCreateGroupChat(conversationId: String)
}