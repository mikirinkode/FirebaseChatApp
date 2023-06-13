package com.mikirinkode.firebasechatapp.feature.chat

import android.app.Activity
import com.mikirinkode.firebasechatapp.base.BasePresenter

interface ConversationPresenterInterface<V>: BasePresenter<V> {
    fun attachView(view: V, mActivity: Activity, conversationId: String, conversationType: String)
}