package com.mikirinkode.firebasechatapp.feature.groupprofile

import com.mikirinkode.firebasechatapp.data.model.UserAccount

interface GroupProfileView {
    fun onParticipantsDataReceived(participants: List<UserAccount>)
}