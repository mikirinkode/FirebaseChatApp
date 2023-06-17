package com.mikirinkode.firebasechatapp.feature.group

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

// TODO: confusing name, because double
class CreateChatHelper(
    private val mListener: CreateChatListener,
) {

    private val storage = FirebaseProvider.instance().getStorage()
    private val database = FirebaseProvider.instance().getDatabase()
    private val conversationsRef = database?.getReference("conversations")
    private val usersRef = database?.getReference("users")

    fun createGroupChat(
        groupName: String,
        participants: List<String>,
        createdBy: String,
        file: Uri?,
        path: String,
    ) {
        Log.e("GCH", "onCreateGroupChat")
        val conversationId = conversationsRef?.push()?.key
        Log.e("GCH", "conversationId: ${conversationId}")


        if (conversationId != null) {
            // add conversationId to all participants
            val unreadMessageCountMap = mutableMapOf<String, Int>()
            for (userId in participants) {
                unreadMessageCountMap[userId] = 0
                Log.e("GCH", "userId: ${userId}")
                usersRef?.child(userId)?.child("conversationIdList")?.child(conversationId) // TODO
                    ?.setValue(mapOf(conversationId to true))
            }

            val timeStamp = System.currentTimeMillis()

            val initialMessage = ChatMessage(
                messageId = "",
                message = "Group Created",
                sendTimestamp = timeStamp,
                type = MessageType.TEXT.toString(),
                senderId = "",
                senderName = "",
                deliveredTimestamp = 0L
            )

            val conversation = Conversation(
                conversationId = conversationId,
                participants = participants,
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

                        conversation.conversationAvatar = uri.toString() // TODO check
                        conversationsRef?.child(conversationId)?.setValue(conversation)
                        mListener.onSuccessCreateGroupChat(conversationId)
                        Log.e("GCH", "successfully created")
                    }
                }
            } else {
                conversationsRef?.child(conversationId)?.setValue(conversation)
                mListener.onSuccessCreateGroupChat(conversationId)
                Log.e("GCH", "successfully created")
            }
        }
    }
}

interface CreateChatListener {
    fun onSuccessCreateGroupChat(conversationId: String)
}