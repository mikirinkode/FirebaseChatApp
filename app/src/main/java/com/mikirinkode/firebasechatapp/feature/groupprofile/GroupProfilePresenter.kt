package com.mikirinkode.firebasechatapp.feature.groupprofile

import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.feature.userlist.UserListView

class GroupProfilePresenter:BasePresenter<GroupProfileView>, GroupProfileListener {

    private var mView: GroupProfileView? = null
    private val mHelper: GroupProfileHelper = GroupProfileHelper(this)

    fun getAllParticipantsDate(participants: List<String>){
        mHelper.getParticipantList(participants)
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