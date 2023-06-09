package com.mikirinkode.firebasechatapp.feature.chat

import com.mikirinkode.firebasechatapp.base.BasePresenter
import com.mikirinkode.firebasechatapp.data.model.Conversation

class GroupChatPresenter: BasePresenter<GroupChatView>, GroupChatListener
{
    private var mView: GroupChatView? = null
    private var groupChatHelper: GroupChatHelper? = null

    fun onInit(conversationId: String){
        groupChatHelper = GroupChatHelper(this, conversationId)
    }

    fun getGroupData(conversationId: String){
        groupChatHelper?.getConversationData(conversationId)
    }

    override fun onReceiveGroupData(conversation: Conversation) {
        mView?.onReceiveGroupData(conversation)
    }

    override fun attachView(view: GroupChatView) {
        mView = view
    }

    override fun detachView() {
        groupChatHelper = null
        mView = null
    }

}
