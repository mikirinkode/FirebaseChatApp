package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper

class ChatHelper(
    private val mListener: ChatEventListener
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val storage = FirebaseHelper.instance().getStorage()
    private val firestore = FirebaseHelper.instance().getFirestore()

    private val chatMessagesRef = database?.getReference("conversations")
//    private val messageRef = database?.getReference("messages")

    fun sendMessage(message: String, senderId: String, receiverId: String) {
        val conversationId = if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
        val timeStamp = System.currentTimeMillis()
        val chatMessage = ChatMessage(
            message, "", timeStamp, senderId, receiverId
        )

        val conversation = hashMapOf(
            "conversationId" to conversationId,
            "userIdList" to listOf(senderId, receiverId),
            "lastMessage" to message,
            "lastMessageTimestamp" to timeStamp,
            "lastSenderId" to senderId
        )

        chatMessagesRef?.child(conversationId)?.updateChildren(conversation)
        chatMessagesRef?.child(conversationId)?.child("messages")?.push()?.setValue(chatMessage)

//        val ref = firestore?.collection("conversations")?.document(conversationId)

//        ref?.set(conversation, SetOptions.merge())
    }

    fun sendMessage(message: String, senderId: String, receiverId: String, file: Uri, path: String) {
        val sRef: StorageReference? = storage?.reference?.child(path)
        val conversationId = if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"

        sRef?.putFile(file)?.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val timeStamp = System.currentTimeMillis()
                val chatMessage = ChatMessage(
                    message, imageUrl = uri.toString(), timeStamp, senderId, receiverId
                )
                val conversation = hashMapOf(
                    "conversationId" to conversationId,
                    "userIdList" to listOf(senderId, receiverId),
                    "lastMessage" to message,
                    "lastMessageTimestamp" to timeStamp,
                    "lastSenderId" to senderId
                )
                chatMessagesRef?.child(conversationId)?.updateChildren(conversation)
                chatMessagesRef?.child(conversationId)?.child("messages")?.push()?.setValue(chatMessage)

//                val ref = firestore?.collection("conversations")?.document(conversationId)
//
//                ref?.set(conversation, SetOptions.merge())
            }
        }

    }

    fun receiveMessages(openedChatUserId: String, loggedUserId: String) {
        val conversationId = if (openedChatUserId < loggedUserId) "$openedChatUserId-$loggedUserId" else "$loggedUserId-$openedChatUserId"

        chatMessagesRef?.child(conversationId)?.child("messages")?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messages = mutableListOf<ChatMessage>()
                for (snapshot in dataSnapshot.children){
                    val chatMessage = snapshot.getValue(ChatMessage::class.java)
                    if (chatMessage != null && chatMessage.receiverId == loggedUserId && chatMessage.senderId == openedChatUserId){
                        messages.add(chatMessage)
                    }
                    if (chatMessage != null && chatMessage.receiverId == openedChatUserId && chatMessage.senderId == loggedUserId){
                        messages.add(chatMessage)
                    }
                }
                val sortedMessages = messages.sortedBy { it.timestamp }
                mListener.onDataChangeReceived(sortedMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO
            }
        })
    }
}

interface ChatEventListener {
    fun onDataChangeReceived(messages: List<ChatMessage>)
}