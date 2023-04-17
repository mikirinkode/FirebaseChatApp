package com.mikirinkode.firebasechatapp.feature.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.firebase.auth.EmailLoginHelper
import com.mikirinkode.firebasechatapp.firebase.auth.EmailLoginListener
import com.mikirinkode.firebasechatapp.firebase.auth.GoogleAuthHelper
import com.mikirinkode.firebasechatapp.firebase.auth.GoogleAuthListener

class LoginPresenter(): BasePresenter<LoginView>, EmailLoginListener, GoogleAuthListener {
    private var view: LoginView? = null
    private val emailLoginHelper = EmailLoginHelper(mListener = this)

    fun performSignIn(email: String, password: String) {
        Log.e("LoginPresenter", "login performSignIn")
        emailLoginHelper.performLogin(email, password)
    }

    fun performSignInGoogle(mActivity: Activity){
        val googleAuthHelper = GoogleAuthHelper(mActivity, this)
        googleAuthHelper.performSignIn()
    }

    fun onActivityResult(mActivity: Activity, requestCode: Int, resultCode: Int, data: Intent){
        val googleAuthHelper = GoogleAuthHelper(mActivity, this)
        googleAuthHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onEmailLoginSuccess(userId: String?) {
        Log.e("LoginPresenter", "login onEmailSignInSuccess")
        view?.onLoginSuccess()
    }

    override fun onEmailLoginFail(errorMessage: String?) {
        Log.e("LoginPresenter", "login onEmailSignInFail")
        view?.onLoginFailed(errorMessage.toString())
    }

    override fun onGoogleAuthSignIn(authToken: String?, userId: String?) {
        view?.onLoginSuccess()
    }

    override fun onGoogleAuthSignInFailed(errorMessage: String?) {
        view?.onLoginFailed(errorMessage.toString())
    }

    override fun attachView(view: LoginView) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}