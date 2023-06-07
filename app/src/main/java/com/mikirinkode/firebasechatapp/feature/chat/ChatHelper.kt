package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class ChatHelper(
    private val mListener: ChatEventListener,
    private val loggedUserId: String,
    private val interlocutorId: String,
) {
    private val database = FirebaseProvider.instance().getDatabase()
    private val storage = FirebaseProvider.instance().getStorage()

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val usersRef = database?.getReference("users")

    private val receiveListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val messages = mutableListOf<ChatMessage>()
            val theLatestMessage =
                dataSnapshot.children.lastOrNull()?.getValue(ChatMessage::class.java)

            for (snapshot in dataSnapshot.children) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {

                    if (chatMessage.receiverId == loggedUserId && chatMessage.senderId == interlocutorId) {

                        messages.add(chatMessage)
                        if (!chatMessage.beenRead) {
                            val timeStamp = System.currentTimeMillis()
                            updateMessageReadTime(timeStamp, snapshot?.key ?: "")

                            if (chatMessage.messageId == theLatestMessage?.messageId) {
                                updateLastMessageReadTime(timeStamp)
                                updateTotalUnreadMessages()
                            }
                        }
                    }
                    if (chatMessage.receiverId == interlocutorId && chatMessage.senderId == loggedUserId) {
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


    fun sendMessage(
        message: String,
        senderId: String,
        receiverId: String,
        senderName: String,
        receiverName: String,
        isFirstTime: Boolean
    ) {
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
            val updateLastMessage = mapOf(
                "lastMessage" to chatMessage
            )

            // push conversation id
            if (isFirstTime) {
                usersRef?.child(receiverId)?.child("conversationIdList")?.child(conversationId)
                    ?.setValue(mapOf(conversationId to true))
                usersRef?.child(senderId)?.child("conversationIdList")?.child(conversationId)
                    ?.setValue(mapOf(conversationId to true))

                val initialConversation = mapOf(
                    "conversationId" to conversationId,
                    "participants" to listOf(senderId, receiverId),
                    "unreadMessages" to 0
                )
                conversationsRef?.child(conversationId)?.updateChildren(initialConversation)
            }

            // TODO: Check again later
            Log.e("ChatHelper", "before on doTransaction")
            // update total unread messages
            conversationsRef?.child(conversationId)?.child("unreadMessages")
                ?.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        Log.e("ChatHelper", "on doTransaction")
                        val currentValue = currentData.getValue(Int::class.java)
                        return if (currentValue != null) {
                            currentData.value = currentValue + 1
                            Transaction.success(currentData)
                        } else {
                            currentData.value = 1
                            Transaction.success(currentData)
                        }
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            // Handle the error
                            Log.e("ChatHelper", "Error incrementing data: ${error.message}")
                        } else if (committed) {
                            // Data incremented successfully
                            Log.e("ChatHelper", "Successfully increment")
                        } else {
                            // Transaction was not committed, handle the case if needed
                        }
                    }
                })

            // push last message
            conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

            // push message
            messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)
        }

    }

    fun sendMessage( // TODO: later
        message: String,
        senderId: String,
        receiverId: String, senderName: String, receiverName: String,
        file: Uri,
        path: String,
        isFirstTime: Boolean
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
                    val updateLastMessage = mapOf(
                        "lastMessage" to chatMessage
                    )

                    // push conversation id
                    if (isFirstTime) {
                        usersRef?.child(receiverId)?.child("conversationIdList")?.child(conversationId)
                            ?.setValue(mapOf(conversationId to true))
                        usersRef?.child(senderId)?.child("conversationIdList")?.child(conversationId)
                            ?.setValue(mapOf(conversationId to true))

                        val initialConversation = mapOf(
                            "conversationId" to conversationId,
                            "participants" to listOf(senderId, receiverId),
                            "unreadMessages" to 0
                        )
                        conversationsRef?.child(conversationId)?.updateChildren(initialConversation)
                    }


                                    Log.e("ChatHelper", "before on doTransaction")
                    // update total unread messages
                    conversationsRef?.child(conversationId)?.child("unreadMessages")
                        ?.runTransaction(object : Transaction.Handler {
                            override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    Log.e("ChatHelper", "on doTransaction")
                                val currentValue = currentData.getValue(Int::class.java)
                                return if (currentValue != null) {
                                    currentData.value = currentValue + 1
                                    Transaction.success(currentData)
                                } else {
                                    currentData.value = 1
                                    Transaction.success(currentData)
                                }
                            }

                            override fun onComplete(
                                error: DatabaseError?,
                                committed: Boolean,
                                currentData: DataSnapshot?
                            ) {
                                if (error != null) {
                                    // Handle the error
                                    Log.e("ChatHelper", "Error incrementing data: ${error.message}")
                                } else if (committed) {
                                    // Data incremented successfully
                                    Log.e("ChatHelper", "Successfully increment")
                                } else {
                                    // Transaction was not committed, handle the case if needed
                                }
                            }
                        })

                    // push last message
                    conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

                    // push message
                    messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)
                }
            }
        }?.addOnProgressListener { taskSnapshot ->
            // Update the progress bar as the upload progresses
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            mListener.showUploadImageProgress(progress)
        }

    }

    fun receiveMessages() {
        val conversationId =
            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"

        messagesRef?.child(conversationId)?.addValueEventListener(receiveListener)
    }

    fun deactivateListener() {
        val conversationId =
            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"

        messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
    }


    private fun updateMessageReadTime(timeStamp: Long, messageId: String) {

        val conversationId =
            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"

        val messageRef =
            messagesRef?.child(conversationId)?.child(messageId)?.ref
        messageRef?.child("readTimestamp")?.setValue(timeStamp)
        messageRef?.child("beenRead")?.setValue(true)
    }

    private fun updateLastMessageReadTime(timeStamp: Long) {
        val conversationId =
            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"

        val conversationRef = conversationsRef?.child(conversationId)?.child("lastMessage")?.ref
        val update = mapOf("readTimestamp" to timeStamp, "beenRead" to true)
        conversationRef?.updateChildren(update)
    }

    // Reset the total unread message
    private fun updateTotalUnreadMessages(){
        val conversationId =
            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
        conversationsRef?.child(conversationId)?.child("unreadMessages")?.setValue(0)
    }
}

interface ChatEventListener {
    fun onDataChangeReceived(messages: List<ChatMessage>)
    fun showUploadImageProgress(progress: Int)
}