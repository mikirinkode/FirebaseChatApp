package com.mikirinkode.firebasechatapp.feature.chat.groupprofile

import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface GroupProfileView {
    fun onParticipantsDataReceived(participants: List<UserAccount>)
    fun onReceiveGroupData(conversation: Conversation)
}