package com.mikirinkode.firebasechatapp.feature.chat

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener

class ChatPresenter: BasePresenter<ChatView>, ChatEventListener, UserOnlineStatusEventListener {
    private var mView: ChatView? = null
    private val chatHelper = ChatHelper(this)
    private val userOnlineStatusHelper = FirebaseUserOnlineStatusHelper(this)

    fun sendMessage(message: String, senderId: String, receiverId: String){
        chatHelper.sendMessage(message, senderId, receiverId)
    }

    fun receiveMessage(receiverId: String, senderId: String){
        chatHelper.receiveMessages(receiverId, senderId)
    }

    override fun onDataChangeReceived(messages: List<ChatMessage>) {
        Log.e("chatpresenter", "messages: $messages")
        Log.e("chatpresenter", "messages: ${messages.size}")
        mView?.updateMessages(messages)
    }

    fun getUserOnlineStatus(userId: String){
        userOnlineStatusHelper.getUserOnlineStatus(userId)
    }

    override fun onUserOnlineStatusReceived(status: UserOnlineStatus) {
        mView?.updateReceiverOnlineStatus(status)
    }

    override fun attachView(view: ChatView) {
        mView = view
    }

    override fun detachView() {
        mView  = null
    }
}