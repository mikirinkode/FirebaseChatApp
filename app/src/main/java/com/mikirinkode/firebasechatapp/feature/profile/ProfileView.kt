package com.mikirinkode.firebasechatapp.feature.profile

import com.mikirinkode.firebasechatapp.base.view.BaseView
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface ProfileView: BaseView {
    fun onGetProfileSuccess(user: UserAccount)
    fun onLogoutSuccess()
}