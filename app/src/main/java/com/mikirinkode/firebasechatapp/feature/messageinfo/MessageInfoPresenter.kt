package com.mikirinkode.firebasechatapp.feature.messageinfo

import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.feature.groupprofile.GroupProfileHelper
import com.mikirinkode.firebasechatapp.feature.groupprofile.GroupProfileView

class MessageInfoPresenter: BasePresenter<MessageInfoView>, MessageInfoListener {

    private var mView: MessageInfoView? = null
    private val mHelper: MessageInfoHelper = MessageInfoHelper(this)


    fun getParticipantList(participants: List<String>){
        mHelper.getParticipantList(participants)
    }


    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        mView?.onParticipantsDataReceived(participants)
    }

    override fun attachView(view: MessageInfoView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}