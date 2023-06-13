package com.mikirinkode.firebasechatapp.feature.group

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListListener
import com.mikirinkode.firebasechatapp.commonhelper.CameraHelper
import com.mikirinkode.firebasechatapp.commonhelper.CameraListener

class CreateCreateGroupPresenter: BasePresenter<CreateGroupChatView>, CreateGroupListener, FirebaseUserListListener, CameraListener {
    private var mView: CreateGroupChatView? = null
    private val mHelper: FirebaseUserListHelper = FirebaseUserListHelper(this)
    private val mGroupHelper: CreateGroupHelper = CreateGroupHelper(this)

    fun createGroupChat(
        groupName: String,
        participants: List<String>,
        createdBy: String,
        file: Uri?,
        path: String,
    ){
        mGroupHelper.createGroupChat(  groupName, participants, createdBy, file, path)
    }

    override fun onSuccessCreateGroupChat(conversationId: String) {
        mView?.onSuccessCreateGroupChat(conversationId)
    }

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

    /**
     * CAMERA
     */
    fun takePicture(mActivity: Activity){
        val cameraHelper = CameraHelper(this, mActivity)
        cameraHelper?.dispatchTakePictureIntent()
    }

    override fun onImageCaptured(capturedImage: Uri?) {
        mView?.onImageCaptured(capturedImage)
    }

    override fun attachView(view: CreateGroupChatView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}