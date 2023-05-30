package com.mikirinkode.firebasechatapp.feature.userlist

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListListener

class UserListPresenter: BasePresenter<UserListView>, FirebaseUserListListener {
    private var mView: UserListView? = null
    private val mHelper: FirebaseUserListHelper = FirebaseUserListHelper(this)

    fun getUserList(){
        mView?.showLoading()
        mHelper.getUserList()
        Log.d("UserListPresenter", "getUserList")
    }

    override fun onGetAllUserDataSuccess(users: List<UserAccount>) {
        mView?.hideLoading()
        mView?.setDataToRecyclerView(users)
        Log.d("UserListPresenter", "users: ${users.size}")
    }

    override fun attachView(view: UserListView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}