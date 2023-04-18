package com.mikirinkode.firebasechatapp.feature.chat

import com.mikirinkode.firebasechatapp.base.BasePresenter

class ChatPresenter: BasePresenter<ChatView> {
    private var mView: ChatView? = null



    override fun attachView(view: ChatView) {
        mView = view
    }

    override fun detachView() {
        mView  = null
    }
}