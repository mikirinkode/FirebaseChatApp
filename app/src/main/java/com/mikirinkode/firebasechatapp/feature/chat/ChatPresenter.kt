package com.mikirinkode.firebasechatapp.feature.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListener
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener
import com.mikirinkode.firebasechatapp.helper.CameraHelper
import com.mikirinkode.firebasechatapp.helper.CameraListener

class ChatPresenter : BasePresenter<ChatView>, ChatEventListener, UserOnlineStatusEventListener, CameraListener, FirebaseUserListener {
    private var mView: ChatView? = null
    private var chatHelper: ChatHelper? = null
    private var cameraHelper: CameraHelper? = null
    private var firebaseUserHelper: FirebaseUserHelper? = null
    private val userOnlineStatusHelper = FirebaseUserOnlineStatusHelper(this)

    fun onInit(mActivity: Activity, loggedUserId: String, openedUserId: String){
        chatHelper = ChatHelper(this, loggedUserId, openedUserId)
        cameraHelper = CameraHelper(mActivity, this)
        firebaseUserHelper = FirebaseUserHelper(this)
    }

    /**
     * MESSAGING
     */
    fun sendMessage(
        message: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String
    ) {
        chatHelper?.sendMessage(message, senderId, receiverId, senderName, receiverName)
    }
    fun sendMessage(
        message: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        file: Uri,
        path: String
    ) {
        chatHelper?.sendMessage(message, senderId, receiverId, senderName, receiverName, file, path)
    }
    fun receiveMessage() {
        chatHelper?.receiveMessages()
    }
    override fun onDataChangeReceived(messages: List<ChatMessage>) {
        mView?.onMessagesReceived(messages)
    }

    /**
     * USER PROFILE DATA
     */
    fun getInterlocutorData(userId: String) {
        firebaseUserHelper?.getUserById(userId)
    }

    override fun onGetUserSuccess(user: UserAccount) {
        mView?.onGetInterlocutorProfileSuccess(user)
    }

    /**
     * USER ONLINE STATUS
     */
    fun getUserOnlineStatus(userId: String) {
        userOnlineStatusHelper.getUserOnlineStatus(userId)
    }
    override fun onUserOnlineStatusReceived(status: UserRTDB) {
        mView?.updateReceiverOnlineStatus(status)
    }

    /**
     * CAMERA
     */
    fun takePicture(){
        cameraHelper?.dispatchTakePictureIntent()
    }

    override fun onImageCaptured(capturedImage: Uri?) {
        mView?.onImageCaptured(capturedImage)
    }


    override fun attachView(view: ChatView) {
        mView = view
    }

    override fun detachView() {
        chatHelper?.deactivateListener()
        chatHelper = null
        cameraHelper = null
        mView = null
    }
}