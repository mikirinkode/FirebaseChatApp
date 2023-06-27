package com.mikirinkode.firebasechatapp.feature.main

import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.firebase.common.CommonFirebaseTaskHelper

class MainPresenter : BasePresenter<MainView>, ChatHistoryListener {
    private var mView: MainView? = null
    private val mCommonHelper: CommonFirebaseTaskHelper = CommonFirebaseTaskHelper()
    private val conversationListHelper = ConversationListHelper(this)

    fun updateUserOnlineStatus() {
        mCommonHelper.updateOnlineStatus()
    }

    fun updateOneSignalToken(){
        mCommonHelper.updateOneSignalDeviceToken()
    }

    fun getMessageHistory() {
        conversationListHelper.receiveMessageHistory()
        mView?.showLoading()
    }

    override fun onDataChangeReceived(conversations: List<Conversation>) {
        mView?.hideLoading()
        if (conversations.isNotEmpty()){
            val sortedList = conversations.sortedBy { it.lastMessage?.sendTimestamp }.reversed()
            mView?.onConversationListReceived(sortedList)
        }
    }

    override fun onEmptyConversation() {
        mView?.hideLoading()
    }

    override fun attachView(view: MainView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

}