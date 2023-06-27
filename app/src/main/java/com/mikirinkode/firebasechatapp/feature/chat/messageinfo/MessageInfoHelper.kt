package com.mikirinkode.firebasechatapp.feature.chat.messageinfo

import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class MessageInfoHelper(
    private val mListener: MessageInfoListener
) {

    private val firestore = FirebaseProvider.instance().getFirestore()
    fun getParticipantList(participantsId: List<String>) {
        val userList = ArrayList<UserAccount>()

        firestore?.collection("users")
            ?.whereIn("userId", participantsId)
            ?.get()
            ?.addOnSuccessListener { documentList ->
                for (document in documentList) {
                    if (document != null) {
                        val userAccount: UserAccount = document.toObject()
                        userList.add(userAccount)
                    }
                }
                mListener.onParticipantsDataReceived(userList)
            }
            ?.addOnFailureListener {
                // TODO: on fail
            }
    }
}

interface MessageInfoListener {
    fun onParticipantsDataReceived(participants: List<UserAccount>)
}