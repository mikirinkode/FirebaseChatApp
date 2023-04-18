package com.mikirinkode.firebasechatapp.feature.chat

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter

class ChatPresenter: BasePresenter<ChatView>, ChatEventListener {
    private var mView: ChatView? = null
    private val chatHelper = ChatHelper(this)

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

    override fun attachView(view: ChatView) {
        mView = view
    }

    override fun detachView() {
        mView  = null
    }
}