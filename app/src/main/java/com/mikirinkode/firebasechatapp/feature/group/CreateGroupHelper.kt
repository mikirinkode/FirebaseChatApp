package com.mikirinkode.firebasechatapp.feature.group

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

// TODO: confusing name, because double
class CreateGroupHelper(
    private val mListener: CreateGroupListener,
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
                usersRef?.child(userId)?.child("conversationIdList")?.child(conversationId)
                    ?.setValue(mapOf(conversationId to true))
            }

            val timeStamp = System.currentTimeMillis()

            // initial chat message
            val initialMessage = hashMapOf<String, Any>(
                "messageId" to "",
                "message" to "Group Created",
                "timestamp" to timeStamp,
                "type" to MessageType.TEXT.toString(),
                "senderId" to "",
                "senderName" to "",
                "receiverId" to "",
                "receiverName" to "",
                "deliveredTimestamp" to 0L
            )

            // upload the conversation avatar
            if (file != null && path != "") {
                val sRef: StorageReference? =
                    storage?.reference?.child("conversations/${conversationId}")?.child(path)
                sRef?.putFile(file)?.addOnSuccessListener {
                    it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->

                        val conversation = mapOf(
                            "conversationId" to conversationId,
                            "participants" to participants,
                            "unreadMessageEachParticipant" to unreadMessageCountMap,
                            "conversationType" to "GROUP",
                            "conversationName" to groupName,
                            "conversationAvatar" to uri.toString(),
                            "createdAt" to timeStamp,
                            "createdBy" to createdBy,
                            "lastMessage" to initialMessage
                        )
                        conversationsRef?.child(conversationId)?.updateChildren(conversation)
                        mListener.onSuccessCreateGroupChat(conversationId)
                        Log.e("GCH", "successfully created")
                    }
                }
            } else {
                val timeStamp = System.currentTimeMillis()

                val conversation = mapOf(
                    "conversationId" to conversationId,
                    "participants" to participants,
                    "unreadMessageEachParticipant" to unreadMessageCountMap,
                    "conversationType" to "GROUP",
                    "conversationName" to groupName,
                    "conversationAvatar" to "",
                    "createdAt" to timeStamp,
                    "createdBy" to createdBy,
                    "lastMessage" to initialMessage
                )
                conversationsRef?.child(conversationId)?.updateChildren(conversation)
                mListener.onSuccessCreateGroupChat(conversationId)
                Log.e("GCH", "successfully created")
            }
        }
    }
}

interface CreateGroupListener {
    fun onSuccessCreateGroupChat(conversationId: String)
}