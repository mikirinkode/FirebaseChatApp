package com.mikirinkode.firebasechatapp.feature.selectuser

import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListListener

class SelectUserPresenter: BasePresenter<SelectUserView>, GroupParticipantListener, FirebaseUserListListener {
    private var mView: SelectUserView? = null
    private val mHelper: FirebaseUserListHelper = FirebaseUserListHelper(this)
    private val mGroupHelper: GroupHelper = GroupHelper(this)

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

    override fun attachView(view: SelectUserView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    fun addParticipantsToGroup(conversationId: String, participantsId: List<String>) {
        mGroupHelper.addParticipantsToGroup(conversationId, participantsId)
    }

    override fun onSuccessAddParticipantsToGroup() {
        mView?.onSuccessAddParticipantsToGroup()
    }
}