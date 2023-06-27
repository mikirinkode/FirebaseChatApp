package com.mikirinkode.firebasechatapp.feature.auth.register

import com.mikirinkode.firebasechatapp.base.view.BaseView

interface RegisterView: BaseView {
    fun onRegisterSuccess()
    fun onRegisterFailed(message: String)
}