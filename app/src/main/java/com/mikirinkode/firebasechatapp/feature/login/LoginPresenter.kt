package com.mikirinkode.firebasechatapp.feature.login

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.firebase.auth.EmailLoginHelper
import com.mikirinkode.firebasechatapp.firebase.auth.EmailLoginListener

class LoginPresenter(): BasePresenter<LoginView>, EmailLoginListener {
    private var view: LoginView? = null
    private val emailLoginHelper = EmailLoginHelper(mListener = this)

    fun performSignIn(email: String, password: String) {
        Log.e("LoginPresenter", "login performSignIn")
        emailLoginHelper.performLogin(email, password)
    }

    override fun onEmailLoginSuccess(userId: String?) {
        Log.e("LoginPresenter", "login onEmailSignInSuccess")
        view?.onLoginSuccess()
    }

    override fun onEmailLoginFail(errorMessage: String?) {
        Log.e("LoginPresenter", "login onEmailSignInFail")
        view?.onLoginFailed(errorMessage.toString())
    }

    override fun attachView(view: LoginView) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}