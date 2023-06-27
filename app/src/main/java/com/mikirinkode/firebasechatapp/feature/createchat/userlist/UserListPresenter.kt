package com.mikirinkode.firebasechatapp.feature.createchat.userlist

import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.user.FirebaseUserListHelper
import com.mikirinkode.firebasechatapp.firebase.user.FirebaseUserListListener

class UserListPresenter: BasePresenter<UserListView>, FirebaseUserListListener {
    private var mView: UserListView? = null
    private val mHelper: FirebaseUserListHelper = FirebaseUserListHelper(this)

    fun getUserList(){
        mView?.showLoading()
        mHelper.getUserList()
    }

    override fun onGetAllUserDataSuccess(users: List<UserAccount>) {
        mView?.hideLoading()
        mView?.setDataToRecyclerView(users)
    }

    override fun attachView(view: UserListView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}