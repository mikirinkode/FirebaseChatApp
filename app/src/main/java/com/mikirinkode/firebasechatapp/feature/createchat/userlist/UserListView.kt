package com.mikirinkode.firebasechatapp.feature.createchat.userlist

import com.mikirinkode.firebasechatapp.base.view.BaseView
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface UserListView: BaseView {
    fun setDataToRecyclerView(users: List<UserAccount>)
}