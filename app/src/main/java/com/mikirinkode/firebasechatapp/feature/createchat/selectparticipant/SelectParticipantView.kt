package com.mikirinkode.firebasechatapp.feature.createchat.selectparticipant

import com.mikirinkode.firebasechatapp.base.view.BaseView
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface SelectParticipantView: BaseView {
    fun setDataToRecyclerView(users: List<UserAccount>)
    fun onSuccessAddParticipantsToGroup()
}