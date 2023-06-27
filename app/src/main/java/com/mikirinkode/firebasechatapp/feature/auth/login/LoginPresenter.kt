package com.mikirinkode.firebasechatapp.feature.auth.login

import android.app.Activity
import android.content.Intent
import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.firebase.common.CommonFirebaseTaskHelper
import com.mikirinkode.firebasechatapp.firebase.auth.EmailLoginHelper
import com.mikirinkode.firebasechatapp.firebase.auth.EmailLoginListener
import com.mikirinkode.firebasechatapp.firebase.auth.GoogleAuthHelper
import com.mikirinkode.firebasechatapp.firebase.auth.GoogleAuthListener

class LoginPresenter(): BasePresenter<LoginView>, EmailLoginListener, GoogleAuthListener {
    private var view: LoginView? = null
    private val emailLoginHelper = EmailLoginHelper(mListener = this)
    private val commonFirebaseTaskHelper = CommonFirebaseTaskHelper()

    fun updateUserOnlineStatus(){
        commonFirebaseTaskHelper.updateOnlineStatus()
    }

    fun performSignIn(email: String, password: String) {
        view?.showLoading()
        emailLoginHelper.performLogin(email, password)
    }

    fun performSignInGoogle(mActivity: Activity){
        view?.showLoading()
        val googleAuthHelper = GoogleAuthHelper(mActivity, this)
        googleAuthHelper.performSignIn()
    }

    fun onActivityResult(mActivity: Activity, requestCode: Int, resultCode: Int, data: Intent){
        val googleAuthHelper = GoogleAuthHelper(mActivity, this)
        googleAuthHelper.onActivityResult(requestCode, resultCode, data)
    }

    override fun onEmailLoginSuccess(userId: String?) {
        view?.onLoginSuccess()
        view?.hideLoading()
    }

    override fun onEmailLoginFail(errorMessage: String?) {
        view?.onLoginFailed(errorMessage.toString())
        view?.hideLoading()
    }

    override fun onGoogleAuthSignIn(authToken: String?, userId: String?) {
        view?.onLoginSuccess()
        view?.hideLoading()
    }

    override fun onGoogleAuthSignInFailed(errorMessage: String?) {
        view?.onLoginFailed(errorMessage.toString())
        view?.hideLoading()
    }

    override fun attachView(view: LoginView) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}