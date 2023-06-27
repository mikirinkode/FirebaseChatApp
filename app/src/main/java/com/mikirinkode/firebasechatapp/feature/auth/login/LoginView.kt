package com.mikirinkode.firebasechatapp.feature.auth.login

import com.mikirinkode.firebasechatapp.base.view.BaseView

interface LoginView: BaseView {
    fun onLoginSuccess()
    fun onLoginFailed(message: String)
}