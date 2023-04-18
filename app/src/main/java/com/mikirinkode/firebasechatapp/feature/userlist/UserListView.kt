package com.mikirinkode.firebasechatapp.feature.userlist

import com.mikirinkode.firebasechatapp.base.BaseView
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface UserListView: BaseView {
    fun setDataToRecyclerView(users: List<UserAccount>)
}