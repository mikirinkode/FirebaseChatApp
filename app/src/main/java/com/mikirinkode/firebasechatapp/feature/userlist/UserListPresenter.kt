package com.mikirinkode.firebasechatapp.feature.userlist

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListHelper

class UserListPresenter: BasePresenter<UserListView> {
    private var mView: UserListView? = null
    private val mHelper: FirebaseUserListHelper = FirebaseUserListHelper()

    fun getUserList(){
        mHelper.getUserList()
        Log.d("UserListPresenter", "getUserList")
    }

    override fun attachView(view: UserListView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}