package com.mikirinkode.firebasechatapp.feature.chat

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

// TODO: confusing name
class GroupChatHelper(
    private val mListener: GroupChatListener,
    private val conversationId: String,
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

                }
            }
            val sortedMessages = messages.sortedBy { it.timestamp }
//            mListener.onDataChangeReceived(sortedMessages)
        }

        override fun onCancelled(error: DatabaseError) {
            // TODO
        }
    }

    fun receiveGroupMessage() {
        val ref = messagesRef?.child(conversationId)
        ref?.keepSynced(true)

        ref?.addValueEventListener(receiveListener)
    }

    fun deactivateListener() {
        messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
    }

    fun getConversationData(conversationId: String) {

        Log.e("GroupChatHelper", "get group data conversationId: $conversationId")
        Log.e("GroupChatHelper", "conversationId: $conversationId")
        val refWithQuery = conversationsRef?.child(conversationId)

        refWithQuery?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(conversationSnapshot: DataSnapshot) {

                Log.e("GroupChatHelper", "onDataChange")
                Log.e("GroupChatHelper", "conversation: $conversationSnapshot")
                val conversation = conversationSnapshot.getValue(Conversation::class.java)
                Log.e("GroupChatHelper", "conversation: $conversation")
                if (conversation != null) {
                    mListener.onReceiveGroupData(conversation)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }
        })
    }
}

interface GroupChatListener {
    fun onReceiveGroupData(conversation: Conversation)
}