package com.mikirinkode.firebasechatapp.feature.main

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus
import com.mikirinkode.firebasechatapp.firebase.CommonFirebaseTaskHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener

class MainPresenter : BasePresenter<MainView>, ChatHistoryListener {
    private var mView: MainView? = null
    private val mCommonHelper: CommonFirebaseTaskHelper = CommonFirebaseTaskHelper()
    private val mainHelper = MainHelper(this)

    fun updateUserOnlineStatus() {
        mCommonHelper.updateUserOnlineStatus()
    }

    fun getMessageHistory() {
        mainHelper.receiveMessageHistory()
    }

    override fun onDataChangeReceived(conversations: List<Conversation>) {
        mView?.onChatHistoryReceived(conversations)
        Log.e("MainPresenter", "chat history received")
        Log.e("MainPresenter", "${conversations.size}")
        for (conversation in conversations) {
            Log.e("MainPresenter", "${conversation.conversationId}")
        }
    }

    override fun attachView(view: MainView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

}