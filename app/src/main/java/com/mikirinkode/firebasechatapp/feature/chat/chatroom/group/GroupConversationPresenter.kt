package com.mikirinkode.firebasechatapp.feature.chat.chatroom.group

import android.app.Activity
import android.net.Uri
import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.user.OnlineStatusHelper
import com.mikirinkode.firebasechatapp.firebase.user.OnlineStatusListener
import com.mikirinkode.firebasechatapp.commonhelper.CameraHelper
import com.mikirinkode.firebasechatapp.commonhelper.CameraListener
import com.mikirinkode.firebasechatapp.firebase.common.CommonFirebaseTaskHelper

class GroupConversationPresenter(
    private val conversationId: String,
    private val mActivity: Activity,
) : BasePresenter<GroupConversationView>, GroupConversationListener, CameraListener {
    private var mView: GroupConversationView? = null
    private var groupConversationHelper: GroupConversationHelper? = GroupConversationHelper(this, conversationId)
    private var cameraHelper: CameraHelper? = CameraHelper(this, mActivity)
    private var mCommonHelper: CommonFirebaseTaskHelper? = CommonFirebaseTaskHelper()

    fun resetTotalUnreadMessage(){
        groupConversationHelper?.resetTotalUnreadMessage()
    }


    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        receiverDeviceTokenList: List<String>
    ) {
        groupConversationHelper?.sendMessage(message, senderId, senderName, receiverDeviceTokenList)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        file: Uri,
        path: String,
        receiverDeviceTokenList: List<String>
    ) {
        groupConversationHelper?.sendMessage(
            message,
            senderId,
            senderName,
            file,
            path,
            receiverDeviceTokenList
        )
    }

    fun receiveMessage() {
        groupConversationHelper?.receiveMessages()
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
        groupConversationHelper?.getConversationById(conversationId)
    }

    override fun onConversationDataReceived(conversation: Conversation) {
        mView?.onConversationDataReceived(conversation)
    }

    /**
     * USER STATUS
     */

    fun updateTypingStatus(isTyping: Boolean, conversationId: String) {
        mCommonHelper?.updateTypingStatus(isTyping, conversationId)
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

    override fun attachView(view: GroupConversationView) {
        mView = view
    }

    override fun detachView() {
        groupConversationHelper?.deactivateListener()
        groupConversationHelper = null
        cameraHelper = null
        mCommonHelper = null
        mView = null
    }
}