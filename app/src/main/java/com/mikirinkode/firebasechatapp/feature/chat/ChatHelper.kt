package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class ChatHelper(
    private val mListener: ChatEventListener,
    private val loggedUserId: String,
    private val openedChatUserId: String,
) {
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
    private val database = FirebaseProvider.instance().getDatabase()
    private val storage = FirebaseProvider.instance().getStorage()
    private val firestore = FirebaseProvider.instance().getFirestore()

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val usersRef = database?.getReference("users")

    private val receiveListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val messages = mutableListOf<ChatMessage>()
            for (snapshot in dataSnapshot.children) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {

                    if (chatMessage.receiverId == loggedUserId && chatMessage.senderId == openedChatUserId) {

                        messages.add(chatMessage)
                        if (!chatMessage.beenRead) {
                            updateMessageReadTime(snapshot?.key ?: "")
                        }
                    }
                    if (chatMessage.receiverId == openedChatUserId && chatMessage.senderId == loggedUserId) {
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


    fun sendMessage(message: String, senderId: String, receiverId: String, senderName: String, receiverName: String) {
        val conversationId =
            if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
        val timeStamp = System.currentTimeMillis()

        val newMessageKey = conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
        if (newMessageKey != null) {
//            val chatMessage = ChatMessage(
//                messageId = newMessageKey,
//                message = message,
//                timestamp = timeStamp,
//                type = MessageType.TEXT.toString(),
//                senderId = senderId,
//                senderName = senderName,
//                receiverId = receiverId,
//                receiverName = receiverName,
//                deliveredTimestamp = 0L,
//                readTimestamp = 0L,
//                beenRead = false,
//            )

            val chatMessage = hashMapOf<String, Any>(
                "messageId" to newMessageKey,
                "message" to message,
                "timestamp" to timeStamp,
                "type" to MessageType.TEXT.toString(),
                "senderId" to senderId,
                "senderName" to senderName,
                "receiverId" to receiverId,
                "receiverName" to receiverName,
                "deliveredTimestamp" to 0L,
                "readTimestamp" to 0L,
                "beenRead" to false,
            )

            // push conversation id
            val isFirstTime = true // TODO
            if (isFirstTime){
                usersRef?.child(receiverId)?.child("conversationIdList")?.setValue(mapOf(conversationId to true))
                usersRef?.child(senderId)?.child("conversationIdList")?.setValue(mapOf(conversationId to true))
            }

            val conversation = hashMapOf<String, Any>(
                "conversationId" to conversationId,
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to chatMessage
            )

            // push last message
            conversationsRef?.child(conversationId)?.updateChildren(conversation)

            // push message
            messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)
        }

    }

    fun sendMessage( // TODO: later
        message: String,
        senderId: String,
        receiverId: String, senderName: String, receiverName: String,
        file: Uri,
        path: String
    ) {
        val sRef: StorageReference? = storage?.reference?.child(path)
        val conversationId =
            if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"

        sRef?.putFile(file)?.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val timeStamp = System.currentTimeMillis()

                val newMessageKey =
                    conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
                if (newMessageKey != null) {
//                    val chatMessage = ChatMessage(
//                        messageId = newMessageKey,
//                        message = message,
//                        imageUrl = uri.toString(),
//                        timestamp = timeStamp,
//                        type = MessageType.IMAGE.toString(),
//                        senderId = senderId,
//                        receiverId = receiverId,
//                        senderName = senderName,
//                        receiverName = receiverName,
//                        deliveredTimestamp = 0L,
//                        readTimestamp = 0L,
//                        beenRead = false,
//                    )

                    val isFirstTime = true
                    if (isFirstTime){
                        usersRef?.child(receiverId)?.child("conversationIdList")?.setValue(mapOf(conversationId to true))
                        usersRef?.child(senderId)?.child("conversationIdList")?.setValue(mapOf(conversationId to true))
                    }

                    val chatMessage = hashMapOf<String, Any>(
                        "messageId" to newMessageKey,
                        "message" to message,
                        "imageUrl" to uri.toString(),
                        "timestamp" to timeStamp,
                        "type" to MessageType.IMAGE.toString(),
                        "senderId" to senderId,
                        "senderName" to senderName,
                        "receiverId" to receiverId,
                        "receiverName" to receiverName,
                        "deliveredTimestamp" to 0L,
                        "readTimestamp" to 0L,
                        "beenRead" to false,
                    )
                    val conversation = hashMapOf(
                        "conversationId" to conversationId,
                        "participants" to listOf(senderId, receiverId),
                        "lastMessage" to chatMessage
                    )

                    // push last message
                    conversationsRef?.child(conversationId)?.updateChildren(conversation)

                    // push message
                    messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)
                }
            }
        }

    }

    fun receiveMessages() {
        val conversationId =
            if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"

        messagesRef?.child(conversationId)?.addValueEventListener(receiveListener)
    }

    fun deactivateListener() {
        val conversationId =
            if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"

        messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
    }


    private fun updateMessageReadTime(messageId: String) {

        val conversationId =
            if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"
        val timeStamp = System.currentTimeMillis()

        val messageRef =
            messagesRef?.child(conversationId)?.child(messageId)?.ref
        messageRef?.child("readTimestamp")?.setValue(timeStamp)
        messageRef?.child("beenRead")?.setValue(true)

    }
}

interface ChatEventListener {
    fun onDataChangeReceived(messages: List<ChatMessage>)
}