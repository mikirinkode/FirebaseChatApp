package com.mikirinkode.firebasechatapp.feature.chat.presenter

import android.app.Activity
import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserHelper
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserListener
import com.mikirinkode.firebasechatapp.firebase.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.UserOnlineStatusEventListener
import com.mikirinkode.firebasechatapp.commonhelper.CameraHelper
import com.mikirinkode.firebasechatapp.commonhelper.CameraListener
import com.mikirinkode.firebasechatapp.feature.chat.helper.ChatEventListener
import com.mikirinkode.firebasechatapp.feature.chat.helper.ChatHelper
import com.mikirinkode.firebasechatapp.feature.chat.ChatView
import com.mikirinkode.firebasechatapp.feature.chat.ConversationPresenterInterface

class ConversationPresenter : BasePresenter<ChatView>, ChatEventListener,
    UserOnlineStatusEventListener, CameraListener, FirebaseUserListener {
    private var mView: ChatView? = null
    private var chatHelper: ChatHelper? = null
    private var cameraHelper: CameraHelper? = null
    private var firebaseUserHelper: FirebaseUserHelper? = null
    private var userOnlineStatusHelper: FirebaseUserOnlineStatusHelper? = null

    // TODO
//    fun onInit(mActivity: Activity, conversationId: String?){
//        chatHelper = ChatHelper(this, conversationId)
//        cameraHelper = CameraHelper(mActivity, this)
//        firebaseUserHelper = FirebaseUserHelper(this)
//    }

    /**
     * MESSAGING
     */
//    fun sendMessage(
//        message: String,
//        senderId: String,
//        receiverId: String,
//        senderName: String,
//        isFirstTime: Boolean
//    ) {
//        if (isFirstTime) {
//            chatHelper?.createPersonaChatRoom(senderId, receiverId)
//        }
//        chatHelper?.sendMessage(message, senderId, senderName)
//    }

    fun createPersonaChatRoom(userId: String, anotherUserId: String) {
        chatHelper?.createPersonaChatRoom(userId, anotherUserId)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
    ) {
        chatHelper?.sendMessage(message, senderId, senderName)
    }

    //    fun sendMessage(
//        message: String,
//        senderId: String,
//        receiverId: String,
//        senderName: String,
//        receiverName: String,
//        isFirstTime: Boolean
//    ) {
//        chatHelper?.sendMessage(message, senderId, receiverId, senderName, receiverName, isFirstTime)
//    }
    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        file: Uri,
        path: String
    ) {
        chatHelper?.sendMessage(
            message,
            senderId,
            senderName,
            file,
            path
        )
    }


    fun receiveMessage() {
        chatHelper?.receiveMessages()
    }

    override fun onDataChangeReceived(messages: List<ChatMessage>) {
        mView?.onMessagesReceived(messages)
    }

    override fun showUploadImageProgress(progress: Int) {
        mView?.showOnUploadImageProgress(progress)
    }

    /**
     * GROUP CHAT DATA
     */
    fun getGroupData(conversationId: String) {
        chatHelper?.getGroupData(conversationId)
    }

    override fun onReceiveGroupData(conversation: Conversation) {
        mView?.onReceiveGroupData(conversation)
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
        userOnlineStatusHelper?.getUserOnlineStatus(userId)
    }

    override fun onUserOnlineStatusReceived(status: UserRTDB) {
        mView?.updateReceiverOnlineStatus(status)
    }

    /**
     * CAMERA
     */
    fun takePicture() {
        cameraHelper?.dispatchTakePictureIntent()
    }

    override fun onImageCaptured(capturedImage: Uri?) {
        mView?.onImageCaptured(capturedImage)
    }


    fun attachView(view: ChatView, mActivity: Activity, conversationId: String, conversationType: String) {
        attachView(view)
        chatHelper = ChatHelper(this, conversationId, conversationType)
        cameraHelper = CameraHelper(this, mActivity)
        firebaseUserHelper = FirebaseUserHelper(this)
        userOnlineStatusHelper = FirebaseUserOnlineStatusHelper(this)

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