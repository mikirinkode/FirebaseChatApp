package com.mikirinkode.firebasechatapp.feature.main

import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus
import com.mikirinkode.firebasechatapp.firebase.CommonFirebaseTaskHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener

class MainPresenter: BasePresenter<MainView> {
    private var mView: MainView? = null
    private val mHelper: CommonFirebaseTaskHelper = CommonFirebaseTaskHelper()

    fun updateUserOnlineStatus(){
        mHelper.updateUserOnlineStatus()
    }

    override fun attachView(view: MainView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

}