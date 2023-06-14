package com.mikirinkode.firebasechatapp.feature.chat.helper

import android.net.Uri
import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlin.math.log

class ChatHelper(
    private val mListener: ChatEventListener,
    private val conversationId: String,
    private val conversationType: String,
) {
    private val database = FirebaseProvider.instance().getDatabase()
    private val storage = FirebaseProvider.instance().getStorage()

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val usersRef = database?.getReference("users")
    private val pref = LocalSharedPref.instance()
    private val loggedUser = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    private val participantIdList = ArrayList<String>()

    private val receiveListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val messages = mutableListOf<ChatMessage>()
            val theLatestMessage =
                dataSnapshot.children.lastOrNull()?.getValue(ChatMessage::class.java)

            Log.e("ChatHelper", "message size: ${dataSnapshot.childrenCount}")
            for (snapshot in dataSnapshot.children) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                Log.e("ChatHelper", "message: ${chatMessage?.message}")

                if (chatMessage != null) {
                    if (loggedUser?.userId != null && chatMessage.senderId != loggedUser.userId) {
                        if (!chatMessage.beenReadBy.containsKey(loggedUser.userId!!)) {
                            val previousMessage = messages.lastOrNull()
                            if (previousMessage != null && previousMessage.beenReadBy.isNotEmpty()) {
                                chatMessage.isTheFirstUnreadMessage = true
                            }

                            val timeStamp = System.currentTimeMillis()
                            updateMessageReadTime(
                                timeStamp,
                                loggedUser.userId!!,
                                snapshot?.key ?: ""
                            )

                            if (chatMessage.messageId == theLatestMessage?.messageId) {
                                updateLastMessageReadTime(
                                    loggedUser.userId!!, timeStamp
                                )
                                resetTotalUnreadMessage()
                            }
                        }
                    }
                    Log.e(
                        "ChatHelper",
                        "message: ${chatMessage.message}, is the first unread message: ${chatMessage.isTheFirstUnreadMessage}"
                    )
                    messages.add(chatMessage)
                }
            }
            val sortedMessages = messages.sortedBy { it.sendTimestamp }
            mListener.onDataChangeReceived(sortedMessages)
        }

        override fun onCancelled(error: DatabaseError) {
            // TODO
        }
    }

    fun createPersonaChatRoom(userId: String, anotherUserId: String) {
        val timeStamp = System.currentTimeMillis()
        usersRef?.child(userId)?.child("conversationIdList")?.child(conversationId)
            ?.setValue(mapOf(conversationId to true))
        usersRef?.child(anotherUserId)?.child("conversationIdList")?.child(conversationId)
            ?.setValue(mapOf(conversationId to true))

        // TODO
        val initialConversation = mapOf(
            "conversationId" to conversationId,
            "participants" to listOf(userId, anotherUserId),
            "conversationType" to "PERSONAL",
            "createdAt" to timeStamp
        )
        conversationsRef?.child(conversationId)?.updateChildren(initialConversation)
    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
    ) {
        val timeStamp = System.currentTimeMillis()
        val newMessageKey = conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
        if (newMessageKey != null) {
            val chatMessage = ChatMessage(
                messageId = newMessageKey,
                message = message,
                sendTimestamp = timeStamp,
                type = MessageType.TEXT.toString(),
                senderId = senderId,
                senderName = senderName,
                deliveredTimestamp = 0L,
            )

            val updateLastMessage = mapOf(
                "lastMessage" to chatMessage
            )

            // TODO: Check again later
            // TODO: maybe cause delay
            Log.e("ChatHelper", "before on doTransaction")
            // update total unread messages
            updateTotalUnreadMessages()

            // push last message
            conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

            // push message
            messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)
            Log.e("ChatHelper", "message pushed")
        }

    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        file: Uri,
        path: String,
    ) {
        val sRef: StorageReference? =
            storage?.reference?.child("conversations/${conversationId}")?.child(path)

        sRef?.putFile(file)?.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val timeStamp = System.currentTimeMillis()

                val newMessageKey =
                    conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
                if (newMessageKey != null) {
                    val chatMessage = ChatMessage(
                        messageId = newMessageKey,
                        message = message,
                        imageUrl = uri.toString(),
                        sendTimestamp = timeStamp,
                        type = MessageType.IMAGE.toString(),
                        senderId = senderId,
                        senderName = senderName,
                        deliveredTimestamp = 0L,
                    )


                    val updateLastMessage = mapOf(
                        "lastMessage" to chatMessage
                    )

                    Log.e("ChatHelper", "before on doTransaction")
                    // update total unread messages
                    updateTotalUnreadMessages()

                    // push last message
                    conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

                    // push message
                    messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)
                }
            }
        }?.addOnProgressListener { taskSnapshot ->
            // Update the progress bar as the upload progresses
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            mListener.showUploadImageProgress(progress)
        }
    }

    fun receiveMessages() {
        Log.e("ChatHelper", "ReceiveMessages called")
        Log.e("ChatHelper", "conversationId: ${conversationId}")

        val ref = conversationId?.let { messagesRef?.child(it) }
        ref?.keepSynced(true)

        ref?.addValueEventListener(receiveListener)
    }

    fun getGroupData(conversationId: String) {
        val refWithQuery = conversationsRef?.child(conversationId)

        refWithQuery?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(conversationSnapshot: DataSnapshot) {
                val conversation = conversationSnapshot.getValue(Conversation::class.java)
                if (conversation != null) {
                    participantIdList.clear()
                    participantIdList.addAll(conversation.participants)
                    mListener.onReceiveGroupData(conversation)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }
        })
    }

    fun deactivateListener() {
        if (conversationId != null) {
            messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
        }
    }



    private fun updateMessageReadTime(timeStamp: Long, userId: String, messageId: String) {

        messagesRef?.child(conversationId)?.child(messageId)?.child("beenReadBy")?.child(userId)
            ?.setValue(timeStamp)
    }

    private fun updateLastMessageReadTime(userId: String, timeStamp: Long) {
        conversationsRef?.child(conversationId)?.child("lastMessage")?.child("beenReadBy")
            ?.child(userId)?.setValue(timeStamp)
    }

    // Reset the total unread message
    private fun resetTotalUnreadMessage() {
        loggedUser?.userId?.let {
            conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")?.child(
                it
            )?.setValue(0)
        }
    }

    private fun updateTotalUnreadMessages() {
        if (participantIdList.isEmpty()){
            conversationsRef?.child(conversationId)?.child("participants")?.get()?.addOnSuccessListener {participants ->
                participants.children.forEach { id ->
                    val userId = id.getValue(String::class.java)
                    if (userId != loggedUser?.userId) {
                        if (userId != null) {
                            conversationsRef.child(conversationId).child("unreadMessageEachParticipant")
                                .child(userId).setValue(ServerValue.increment(1))
                        }
                    }
                }
            }
        }
        participantIdList.forEach { userId ->
            if (userId != loggedUser?.userId) {
                conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")
                    ?.child(userId)?.setValue(ServerValue.increment(1))
            }
        }
//        conversationsRef?.child(conversationId)?.child("unreadMessages")
//            ?.runTransaction(object : Transaction.Handler {
//                override fun doTransaction(currentData: MutableData): Transaction.Result {
//                    Log.e("ChatHelper", "on doTransaction")
//                    val currentValue = currentData.getValue(Int::class.java)
//                    return if (currentValue != null) {
//                        currentData.value = currentValue + 1
//                        Transaction.success(currentData)
//                    } else {
//                        currentData.value = 1
//                        Transaction.success(currentData)
//                    }
//                }
//
//                override fun onComplete(
//                    error: DatabaseError?,
//                    committed: Boolean,
//                    currentData: DataSnapshot?
//                ) {
//                    if (error != null) {
//                        // Handle the error
//                        Log.e("ChatHelper", "Error incrementing data: ${error.message}")
//                    } else if (committed) {
//                        // Data incremented successfully
//                        Log.e("ChatHelper", "Successfully increment")
//                    } else {
//                        // Transaction was not committed, handle the case if needed
//                    }
//                }
//            })
    }
}

interface ChatEventListener {
    fun onDataChangeReceived(messages: List<ChatMessage>) // todo: change name
    fun showUploadImageProgress(progress: Int)

    fun onReceiveGroupData(conversation: Conversation)
}