package com.mikirinkode.firebasechatapp.feature.main

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.firebase.CommonFirebaseTaskHelper

class MainPresenter : BasePresenter<MainView>, ChatHistoryListener {
    private var mView: MainView? = null
    private val mCommonHelper: CommonFirebaseTaskHelper = CommonFirebaseTaskHelper()
    private val mainHelper = MainHelper(this)

    fun updateUserOnlineStatus() {
        mCommonHelper.updateOnlineStatus()
    }

    // TODO: show loading
    fun getMessageHistory() {
        Log.e("MainPresenter", "getMessageHistory called")
        mainHelper.receiveMessageHistory()
    }

    override fun onDataChangeReceived(conversations: List<Conversation>) {
        if (conversations.isNotEmpty()){
            mView?.onChatHistoryReceived(conversations)
        }
            Log.e("MainPresenter", "on chat history data change received")
    }

    override fun attachView(view: MainView) {
        mView = view
        mCommonHelper.observeToken()
    }

    override fun detachView() {
        mView = null
    }

}