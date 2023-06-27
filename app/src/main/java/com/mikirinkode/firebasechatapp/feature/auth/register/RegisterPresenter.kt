package com.mikirinkode.firebasechatapp.feature.auth.register

import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.firebase.auth.EmailRegisterHelper
import com.mikirinkode.firebasechatapp.firebase.auth.EmailRegisterListener

class RegisterPresenter: BasePresenter<RegisterView>, EmailRegisterListener {
    private var view: RegisterView? = null
    private val emailRegisterHelper = EmailRegisterHelper(mListener = this)

    fun performRegister(name: String, email: String, password: String) {
        view?.showLoading()
        emailRegisterHelper.performRegister(name, email, password)
    }

    override fun onEmailRegisterSuccess() {
        view?.hideLoading()
        view?.onRegisterSuccess()
    }

    override fun onEmailRegisterFail(errorMessage: String?) {
        view?.hideLoading()
        errorMessage?.let { view?.onRegisterFailed(it) }
    }

    override fun attachView(view: RegisterView) {
        this.view = view
    }

    override fun detachView() {
        view = null
    }
}