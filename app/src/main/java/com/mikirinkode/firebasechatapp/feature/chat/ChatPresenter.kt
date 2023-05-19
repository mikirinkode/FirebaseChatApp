package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener

class ChatPresenter : BasePresenter<ChatView>, ChatEventListener, UserOnlineStatusEventListener {
    private var mView: ChatView? = null
    private var chatHelper: ChatHelper? = null
    private val userOnlineStatusHelper = FirebaseUserOnlineStatusHelper(this)

    fun sendMessage(
        message: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String
    ) {
        chatHelper?.sendMessage(message, senderId, receiverId, senderName, receiverName)
    }

    fun sendMessage( // TODO: add name
        message: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        file: Uri,
        path: String
    ) {
        chatHelper?.sendMessage(message, senderId, receiverId, senderName, receiverName, file, path)
    }

    fun receiveMessage(loggedUserId: String, openedUserId: String) {
        chatHelper = ChatHelper(this, loggedUserId, openedUserId)
        chatHelper?.receiveMessages()
    }

    override fun onDataChangeReceived(messages: List<ChatMessage>) {
        mView?.updateMessages(messages)
    }

    fun getUserOnlineStatus(userId: String) {
        userOnlineStatusHelper.getUserOnlineStatus(userId)
    }

    override fun onUserOnlineStatusReceived(status: UserRTDB) {
        mView?.updateReceiverOnlineStatus(status)
    }

    override fun attachView(view: ChatView) {
        mView = view
    }

    override fun detachView() {
        chatHelper?.deactivateListener()
        chatHelper = null
        mView = null
    }
}