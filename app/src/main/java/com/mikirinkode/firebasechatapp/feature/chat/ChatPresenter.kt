package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener

class ChatPresenter : BasePresenter<ChatView>, ChatEventListener, UserOnlineStatusEventListener {
    private var mView: ChatView? = null
    private var chatHelper: ChatHelper? = null
    private val userOnlineStatusHelper = FirebaseUserOnlineStatusHelper(this)

    fun sendMessage(message: String, senderId: String, receiverId: String) {
        chatHelper?.sendMessage(message, senderId, receiverId)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        receiverId: String,
        file: Uri,
        path: String
    ) {
        chatHelper?.sendMessage(message, senderId, receiverId, file, path)
    }

    fun receiveMessage(loggedUserId: String, openedUserId: String) {
        Log.e("chatpresenter", "received message called")
        Log.e("chatpresenter", "helper: $chatHelper")

        chatHelper = ChatHelper(this, loggedUserId, openedUserId)
        chatHelper?.receiveMessages()
    }

    override fun onDataChangeReceived(messages: List<ChatMessage>) {
        Log.e("chatpresenter", "onDataChangeReceived")
        Log.e("chatpresenter", "messages: ${messages.size}")
        Log.e("chatpresenter", "view: $mView")
        mView?.updateMessages(messages)
    }

    fun getUserOnlineStatus(userId: String) {
        userOnlineStatusHelper.getUserOnlineStatus(userId)
    }

    override fun onUserOnlineStatusReceived(status: UserOnlineStatus) {
        mView?.updateReceiverOnlineStatus(status)
    }

    override fun attachView(view: ChatView) {
        mView = view
    }

    override fun detachView() {
        Log.e("ChatPresenter", "detaching view")

        chatHelper?.deactivateListener()
        chatHelper = null
        mView = null
    }
}