package com.mikirinkode.firebasechatapp.feature.register

import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.firebase.auth.EmailRegisterHelper
import com.mikirinkode.firebasechatapp.firebase.auth.EmailRegisterListener

class RegisterPresenter: BasePresenter<RegisterView>, EmailRegisterListener {
    private var view: RegisterView? = null
    private val emailRegisterHelper = EmailRegisterHelper(mListener = this)

    fun performRegister(email: String, password: String) {
        emailRegisterHelper.performRegister(email, password)
    }

    override fun onEmailRegisterSuccess() {
        view?.onRegisterSuccess()
    }

    override fun onEmailRegisterFail(errorMessage: String?) {
        errorMessage?.let { view?.onRegisterFailed(it) }
    }

    override fun attachView(view: RegisterView) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}