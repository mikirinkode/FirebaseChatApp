package com.mikirinkode.firebasechatapp.firebase.auth

interface EmailRegisterListener {
    fun onEmailRegisterSuccess()
    fun onEmailRegisterFail(errorMessage: String?)
}