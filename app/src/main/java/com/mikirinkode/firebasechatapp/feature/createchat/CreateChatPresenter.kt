package com.mikirinkode.firebasechatapp.feature.createchat

import android.app.Activity
import android.net.Uri
import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.commonhelper.CameraHelper
import com.mikirinkode.firebasechatapp.commonhelper.CameraListener

class CreateChatPresenter: BasePresenter<CreateChatView>, CreateChatListener, CameraListener {
    private var mView: CreateChatView? = null
    private val mGroupHelper: CreateChatHelper = CreateChatHelper(this)

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

    override fun attachView(view: CreateChatView) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }
}