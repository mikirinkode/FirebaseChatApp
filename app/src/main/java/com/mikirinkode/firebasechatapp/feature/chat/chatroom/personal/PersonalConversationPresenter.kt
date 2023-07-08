package com.mikirinkode.firebasechatapp.feature.chat.chatroom.personal

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.mikirinkode.firebasechatapp.base.presenter.BasePresenter
import com.mikirinkode.firebasechatapp.commonhelper.CameraHelper
import com.mikirinkode.firebasechatapp.commonhelper.CameraListener
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.common.CommonFirebaseTaskHelper

class PersonalConversationPresenter(
    private var mView: PersonalConversationView?,
    private var conversationId: String,
    private var interlocutorId: String,
    private var mActivity: Activity
) : BasePresenter<PersonalConversationView>, PersonalConversationListener, CameraListener {
    private val mHelper: PersonalConversationHelper = PersonalConversationHelper(conversationId, interlocutorId, this)
    private var mCommonHelper: CommonFirebaseTaskHelper? = CommonFirebaseTaskHelper()
    private var cameraHelper: CameraHelper? = CameraHelper(this, mActivity)

    /**
     * MESSAGING
     */
    fun createPersonaChatRoom(userId: String, anotherUserId: String) {
        mHelper?.createPersonaChatRoom(userId, anotherUserId)
    }
    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        receiverDeviceTokenList: List<String>
    ) {
        mHelper?.sendMessage(message, senderId, senderName, receiverDeviceTokenList)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        file: Uri,
        path: String,
        receiverDeviceTokenList: List<String>
    ) {
        mHelper?.sendMessage(
            message,
            senderId,
            senderName,
            file,
            path,
            receiverDeviceTokenList
        )
    }
    fun receiveMessage(){
        mHelper.receiveMessage()
    }

    override fun onMessagesReceived(messages: List<ChatMessage>) {
        mView?.onMessagesReceived(messages)
    }
    fun resetTotalUnreadMessage(){
        mHelper?.resetTotalUnreadMessage()
    }


    override fun showUploadImageProgress(progress: Int) {
        mView?.showOnUploadImageProgress(progress)
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


    /**
     * USER DATA
     */
    fun getInterlocutorData(userId: String){
        mHelper.getUserById(userId)
    }

    override fun onInterlocutorDataReceived(user: UserAccount) {
        mView?.onInterlocutorDataReceived(user)
    }


    fun updateTypingStatus(isTyping: Boolean, typingFor: String, conversationId: String) {
        Log.e("PersonalConversationPresenter", "updateTypingStatus called")
        Log.e("PersonalConversationPresenter", "is typing: ${isTyping}")
        Log.e("PersonalConversationPresenter", "conversationId: ${typingFor}")
        mCommonHelper?.updateTypingStatusForPersonalConversation(isTyping, typingFor, conversationId)
    }

    override fun attachView(view: PersonalConversationView) {
        mView = view
    }

    override fun detachView() {
        mView = null
        mHelper.deactivateListener()
    }
}