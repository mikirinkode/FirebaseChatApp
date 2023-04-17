package com.mikirinkode.firebasechatapp.feature.register

import com.mikirinkode.firebasechatapp.base.BaseView

interface RegisterView: BaseView {
    fun onRegisterSuccess()
    fun onRegisterFailed(message: String)
}