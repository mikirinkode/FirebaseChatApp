package com.mikirinkode.firebasechatapp.feature.chat.presenter

import android.app.Activity
import android.net.Uri
import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.user.FirebaseUserHelper
import com.mikirinkode.firebasechatapp.firebase.user.FirebaseUserOnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.user.UserOnlineStatusEventListener
import com.mikirinkode.firebasechatapp.commonhelper.CameraHelper
import com.mikirinkode.firebasechatapp.commonhelper.CameraListener
import com.mikirinkode.firebasechatapp.feature.chat.helper.ChatEventListener
import com.mikirinkode.firebasechatapp.feature.chat.helper.ConversationHelper
import com.mikirinkode.firebasechatapp.feature.chat.ConversationView

class ConversationPresenter : BasePresenter<ConversationView>, ChatEventListener,
    UserOnlineStatusEventListener, CameraListener {
    private var mView: ConversationView? = null
    private var conversationHelper: ConversationHelper? = null
    private var cameraHelper: CameraHelper? = null
    private var firebaseUserHelper: FirebaseUserHelper? = null
    private var userOnlineStatusHelper: FirebaseUserOnlineStatusHelper? = null

    fun resetTotalUnreadMessage(){
        conversationHelper?.resetTotalUnreadMessage()
    }

    fun createPersonaChatRoom(userId: String, anotherUserId: String) {
        conversationHelper?.createPersonaChatRoom(userId, anotherUserId)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        receiverDeviceTokenList: List<String>
    ) {
        conversationHelper?.sendMessage(message, senderId, senderName, receiverDeviceTokenList)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        file: Uri,
        path: String,
        receiverDeviceTokenList: List<String>
    ) {
        conversationHelper?.sendMessage(
            message,
            senderId,
            senderName,
            file,
            path,
            receiverDeviceTokenList
        )
    }

    fun receiveMessage() {
        conversationHelper?.receiveMessages()
    }

    override fun onMessageReceived(messages: List<ChatMessage>) {
        mView?.onMessagesReceived(messages)
    }

    override fun showUploadImageProgress(progress: Int) {
        mView?.showOnUploadImageProgress(progress)
    }

    /**
     * GROUP CHAT DATA
     */
    fun getConversationDataById(conversationId: String) {
        conversationHelper?.getConversationById(conversationId)
    }

    override fun onConversationDataReceived(conversation: Conversation) {
        mView?.onConversationDataReceived(conversation)
    }

    /**
     * USER ONLINE STATUS
     */
    fun getUserOnlineStatus(userId: String) {
        userOnlineStatusHelper?.getUserOnlineStatus(userId)
    }

    override fun onUserOnlineStatusReceived(status: UserAccount) {
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

    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        mView?.onParticipantsDataReceived(participants)
    }

    fun attachView(view: ConversationView, mActivity: Activity, conversationId: String, conversationType: String) {
        attachView(view)
        conversationHelper = ConversationHelper(this, conversationId, conversationType)
        cameraHelper = CameraHelper(this, mActivity)
        userOnlineStatusHelper = FirebaseUserOnlineStatusHelper(this)

    }

    override fun attachView(view: ConversationView) {
        mView = view
    }

    override fun detachView() {
        conversationHelper?.deactivateListener()
        conversationHelper = null
        cameraHelper = null
        mView = null
    }
}