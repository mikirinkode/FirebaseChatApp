package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper

class ChatHelper(
    private val mListener: ChatEventListener,
    private val loggedUserId: String,
    private val openedChatUserId: String,
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val storage = FirebaseHelper.instance().getStorage()
    private val firestore = FirebaseHelper.instance().getFirestore()

    private val conversationsRef = database?.getReference("conversations")

    private val receiveListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val messages = mutableListOf<ChatMessage>()
            for (snapshot in dataSnapshot.children) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                            Log.e("ChatHelper", "message id: ${chatMessage.messageId} --- been read? : ${chatMessage.beenRead}")
                            Log.e("ChatHelper", "logged id: $loggedUserId --- opened Id : $openedChatUserId")
                            Log.e("ChatHelper", "receiver id: ${chatMessage.receiverId} --- sender Id : ${chatMessage.senderId}")

                    if (chatMessage.receiverId == loggedUserId && chatMessage.senderId == openedChatUserId) {
                            Log.e("ChatHelper", "${chatMessage.messageId} you are the receiver")

                        messages.add(chatMessage)
                        if (!chatMessage.beenRead) {
                            Log.e("ChatHelper", "message has not been read, should call update message read time function")
                            updateMessageReadTime(snapshot?.key ?: "")
                            Log.e("ChatHelper", "message has not been read, after call update message read time function")
                        }
                    }
                    if (chatMessage.receiverId == openedChatUserId && chatMessage.senderId == loggedUserId) {
                            Log.e("ChatHelper", "${chatMessage.messageId} you are the sender")
                        messages.add(chatMessage)
                    }
                }
            }
            val sortedMessages = messages.sortedBy { it.timestamp }
            mListener.onDataChangeReceived(sortedMessages)
        }

        override fun onCancelled(error: DatabaseError) {
            // TODO
        }
    }


    fun sendMessage(message: String, senderId: String, receiverId: String) {
        val conversationId =
            if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
        val timeStamp = System.currentTimeMillis()

        val conversation = hashMapOf(
            "conversationId" to conversationId,
            "userIdList" to listOf(senderId, receiverId),
            "lastMessage" to message,
            "lastMessageTimestamp" to timeStamp,
            "lastSenderId" to senderId
        )

        conversationsRef?.child(conversationId)?.updateChildren(conversation)
        val newMessageKey = conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
        if (newMessageKey != null) {
            val chatMessage = ChatMessage(
                messageId = newMessageKey,
                message = message,
                timestamp = timeStamp,
                senderId = senderId,
                receiverId = receiverId,
                deliveredTimestamp = 0L,
                readTimestamp = 0L,
                beenRead = false,
            )
            conversationsRef?.child(conversationId)?.child("messages")?.child(newMessageKey)
                ?.setValue(chatMessage)
        }

//        val ref = firestore?.collection("conversations")?.document(conversationId)

//        ref?.set(conversation, SetOptions.merge())
    }

    fun sendMessage(
        message: String,
        senderId: String,
        receiverId: String,
        file: Uri,
        path: String
    ) {
        val sRef: StorageReference? = storage?.reference?.child(path)
        val conversationId =
            if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"

        sRef?.putFile(file)?.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val timeStamp = System.currentTimeMillis()
                val conversation = hashMapOf(
                    "conversationId" to conversationId,
                    "userIdList" to listOf(senderId, receiverId),
                    "lastMessage" to message,
                    "lastMessageTimestamp" to timeStamp,
                    "lastSenderId" to senderId
                )
                conversationsRef?.child(conversationId)?.updateChildren(conversation)
                val newMessageKey =
                    conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
                if (newMessageKey != null) {
                    val chatMessage = ChatMessage(
                        messageId = newMessageKey,
                        message = message,
                        imageUrl = uri.toString(),
                        timestamp = timeStamp,
                        senderId = senderId,
                        receiverId = receiverId,
                        deliveredTimestamp = 0L,
                        readTimestamp = 0L,
                        beenRead = false,
                    )
                    conversationsRef?.child(conversationId)?.child("messages")?.child(newMessageKey)
                        ?.setValue(chatMessage)
                }

//                val ref = firestore?.collection("conversations")?.document(conversationId)
//
//                ref?.set(conversation, SetOptions.merge())
            }
        }

    }

    fun receiveMessages() {
        val conversationId =
            if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"

        conversationsRef?.child(conversationId)?.child("messages")
            ?.addValueEventListener(receiveListener)
    }

    fun deactivateListener() {
        val conversationId =
            if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"

        Log.e("ChatHelper", "deactivateListener called")
        conversationsRef?.child(conversationId)?.child("messages")
            ?.removeEventListener(receiveListener)
    }

    private fun updateMessageDeliveredTime(conversationId: String, messageId: String) {
        val timeStamp = System.currentTimeMillis()

        val messageRef =
            conversationsRef?.child(conversationId)?.child("messages")?.child(messageId)?.ref
        messageRef?.child("deliveredTimestamp")?.setValue(timeStamp)
    }

    private fun updateMessageReadTime(messageId: String) {
        Log.e("ChatHelper", "updateMessageReadTime called, message id: $messageId")
        val conversationId =
            if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"
        val timeStamp = System.currentTimeMillis()

        val messageRef =
            conversationsRef?.child(conversationId)?.child("messages")?.child(messageId)?.ref
        messageRef?.child("readTimestamp")?.setValue(timeStamp)
        messageRef?.child("beenRead")?.setValue(true)

    }
}

interface ChatEventListener {
    fun onDataChangeReceived(messages: List<ChatMessage>)
}