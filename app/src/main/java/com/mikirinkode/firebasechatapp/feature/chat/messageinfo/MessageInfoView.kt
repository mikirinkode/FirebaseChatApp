package com.mikirinkode.firebasechatapp.feature.chat.messageinfo

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface MessageInfoView {
    fun onParticipantsDataReceived(participants: List<UserAccount>)
}