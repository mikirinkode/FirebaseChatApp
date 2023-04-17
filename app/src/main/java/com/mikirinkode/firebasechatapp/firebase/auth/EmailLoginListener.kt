package com.mikirinkode.firebasechatapp.firebase.auth

interface EmailLoginListener {

    fun onEmailLoginSuccess(userId: String?)
    fun onEmailLoginFail(errorMessage: String?)
}