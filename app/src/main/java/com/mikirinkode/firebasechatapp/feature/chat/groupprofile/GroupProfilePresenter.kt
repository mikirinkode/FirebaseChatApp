package com.mikirinkode.firebasechatapp.feature.chat.groupprofile

import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount

class GroupProfilePresenter: BasePresenter<GroupProfileView>, GroupProfileListener {

    private var mView: GroupProfileView? = null
    private val mHelper: GroupProfileHelper = GroupProfileHelper(this)

    fun getParticipantList(participants: List<String>){
        mHelper.getParticipantList(participants)
    }

    fun getGroupData(conversationId: String) {
        mHelper.getGroupData(conversationId)
    }

    override fun onReceiveGroupData(conversation: Conversation) {
        mView?.onReceiveGroupData(conversation)
    }

    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        mView?.onParticipantsDataReceived(participants)
    }

    override fun attachView(view: GroupProfileView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}