package com.mikirinkode.firebasechatapp.feature.selectuser

import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface SelectUserView: BaseView {
    fun setDataToRecyclerView(users: List<UserAccount>)
    fun onSuccessAddParticipantsToGroup()
}