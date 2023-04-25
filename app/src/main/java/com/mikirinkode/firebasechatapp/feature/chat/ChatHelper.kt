package com.mikirinkode.firebasechatapp.feature.chat

import android.net.Uri
import android.webkit.MimeTypeMap
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper

class ChatHelper(
    private val mListener: ChatEventListener
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val storage = FirebaseHelper.instance().getStorage()

    private val messageRef = database?.getReference("messages")

    fun sendMessage(message: String, senderId: String, receiverId: String) {
        val timeStamp = System.currentTimeMillis()
        val chatMessage = ChatMessage(
            message, "", timeStamp, senderId, receiverId
        )
        messageRef?.push()?.setValue(chatMessage)
    }

    fun sendMessage(message: String, senderId: String, receiverId: String, file: Uri, path: String) {

        val sRef: StorageReference? = storage?.reference?.child(path)

        sRef?.putFile(file)?.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val timeStamp = System.currentTimeMillis()
                val chatMessage = ChatMessage(
                    message, imageUrl = uri.toString(), timeStamp, senderId, receiverId
                )
                messageRef?.push()?.setValue(chatMessage)
            }
        }

    }

    fun receiveMessages(openedChatUserId: String, loggedUserId: String) {
        messageRef?.addValueEventListener(object : ValueEventListener{
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

            }

        })
    }
}

interface ChatEventListener {
    fun onDataChangeReceived(messages: List<ChatMessage>)
}