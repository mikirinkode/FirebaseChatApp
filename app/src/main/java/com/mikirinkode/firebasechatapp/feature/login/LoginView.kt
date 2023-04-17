package com.mikirinkode.firebasechatapp.feature.login

import com.mikirinkode.firebasechatapp.base.BaseView

interface LoginView: BaseView {
    fun onLoginSuccess()
    fun onLoginFailed(message: String)
}