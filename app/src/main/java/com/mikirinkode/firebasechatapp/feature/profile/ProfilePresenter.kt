package com.mikirinkode.firebasechatapp.feature.profile

import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount

class ProfilePresenter : BasePresenter<ProfileView>, ProfileEventListener {
    private var mView: ProfileView? = null
    private var mHelper: ProfileHelper = ProfileHelper(this)

    fun observeUserProfile(userId: String){
        mHelper.getUserById(userId)
    }

    override fun onGetProfileSuccess(user: UserAccount) {
        mView?.onGetProfileSuccess(user)
    }

    override fun attachView(view: ProfileView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}