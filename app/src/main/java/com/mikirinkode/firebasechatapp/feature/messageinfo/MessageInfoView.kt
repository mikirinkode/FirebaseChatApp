package com.mikirinkode.firebasechatapp.feature.messageinfo

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface MessageInfoView {
    fun onParticipantsDataReceived(participants: List<UserAccount>)
}