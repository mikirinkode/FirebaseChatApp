package com.mikirinkode.firebasechatapp.feature.createchat.selectparticipant

import android.util.Log
import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.user.FirebaseUserListHelper
import com.mikirinkode.firebasechatapp.firebase.user.FirebaseUserListListener

class SelectParticipantPresenter: BasePresenter<SelectParticipantView>, AddParticipantListener,
    FirebaseUserListListener {
    private var mView: SelectParticipantView? = null
    private val mHelper: FirebaseUserListHelper = FirebaseUserListHelper(this)
    private val mAddParticipantHelper: AddParticipantHelper = AddParticipantHelper(this)

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

    override fun attachView(view: SelectParticipantView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    fun addParticipantsToGroup(conversationId: String, participantsId: List<String>) {
        mAddParticipantHelper.addParticipantsToGroup(conversationId, participantsId)
    }

    override fun onSuccessAddParticipantsToGroup() {
        mView?.onSuccessAddParticipantsToGroup()
    }
}