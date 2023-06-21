package com.mikirinkode.firebasechatapp.feature.createchat

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class CreateChatHelper(
    private val mListener: CreateChatListener,
) {

    private val storage = FirebaseProvider.instance().getStorage()
    private val database = FirebaseProvider.instance().getDatabase()
    private val conversationsRef = database?.getReference("conversations")
    private val fireStore = FirebaseProvider.instance().getFirestore()

    fun createGroupChat(
        groupName: String,
        participants: List<String>,
        createdBy: String,
        file: Uri?,
        path: String,
    ) {
        val conversationId = conversationsRef?.push()?.key

        if (conversationId != null) {
            // add conversationId to all participants
            val unreadMessageCountMap = mutableMapOf<String, Int>()
            val groupParticipant = mutableMapOf<String, Boolean>()

            for (userId in participants) {
                unreadMessageCountMap[userId] = 0
                groupParticipant[userId] = true
                val userRef = fireStore?.collection("users")?.document(userId)

                userRef?.update("conversationIdList", FieldValue.arrayUnion(conversationId))
            }

            val timeStamp = System.currentTimeMillis()

            val initialMessage = ChatMessage(
                messageId = "",
                message = "Group Created",
                sendTimestamp = timeStamp,
                type = MessageType.TEXT.toString(),
                senderId = "",
                senderName = "",
            )

            val conversation = Conversation(
                conversationId = conversationId,
                participants = groupParticipant,
                lastMessage = initialMessage,
                conversationType = ConversationType.GROUP.toString(),
                conversationAvatar = "",
                conversationName = groupName,
                createdAt = timeStamp,
                createdBy = createdBy,
                unreadMessageEachParticipant = unreadMessageCountMap
            )

            // upload the conversation avatar
            if (file != null && path != "") {
                val sRef: StorageReference? =
                    storage?.reference?.child("conversations/${conversationId}")?.child(path)
                sRef?.putFile(file)?.addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->

                        conversation.conversationAvatar = uri.toString()
                        conversationsRef?.child(conversationId)?.setValue(conversation)
                        mListener.onSuccessCreateGroupChat(conversationId)
                    }
                }
            } else {
                conversationsRef?.child(conversationId)?.setValue(conversation)
                mListener.onSuccessCreateGroupChat(conversationId)
            }
        }
    }
}

interface CreateChatListener {
    fun onSuccessCreateGroupChat(conversationId: String)
}