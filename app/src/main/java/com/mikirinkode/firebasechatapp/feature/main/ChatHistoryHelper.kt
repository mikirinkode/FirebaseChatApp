package com.mikirinkode.firebasechatapp.feature.main

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MainHelper(
    private val mListener: ChatHistoryListener
) {
    private val auth = FirebaseHelper.instance().getFirebaseAuth()
    private val database = FirebaseHelper.instance().getDatabase()
    private val storage = FirebaseHelper.instance().getStorage()
    private val firestore = FirebaseHelper.instance().getFirestore()

    private val conversationsRef = database?.getReference("conversations")

    private suspend fun getUserById(userId: String): MutableList<DocumentSnapshot>? {
        val querySnapshot = firestore?.collection("users")
            ?.whereEqualTo("userId", userId)
            ?.get()?.await()

        return querySnapshot?.documents
    }

    fun receiveMessageHistory() {
        Log.e("ChatHistoryHelper", "receiveMessageHistory called")
        val currentUser = auth?.currentUser

        val conversations = mutableListOf<Conversation>()

        conversationsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("ChatHistoryHelper", "receiveMessageHistory listener on data change")
                conversations.clear()
                for (snapshot in dataSnapshot.children) {
                    val conversationId = snapshot.key?.split("-")

                    if (conversationId?.contains(currentUser?.uid) == true) {
                        val conversation = snapshot.getValue(Conversation::class.java)
                        val firstUserId = conversationId.first()
                        val secondUserId = conversationId.last()

                        val interlocutorId = if (firstUserId == currentUser?.uid) secondUserId else firstUserId

                        runBlocking {

                            val userDoc = getUserById(interlocutorId)
                            val userAccount: UserAccount? = userDoc?.first()?.toObject()
                            var unreadMessageCounter = 0

                            conversation?.messages?.forEach { (key, message) ->
                                if (message.receiverId == currentUser?.uid) {
                                    if (!message.beenRead) {
                                        unreadMessageCounter = unreadMessageCounter.plus(1)
                                    }
                                    if (message.deliveredTimestamp == 0L) {
                                        val timestamp = System.currentTimeMillis()

                                        if (conversation.conversationId != null) {
                                            updateMessageDeliveredTime(
                                                conversation.conversationId!!,
                                                message.messageId,
                                                timestamp
                                            )

//                                            if (message.messageId == conversation.lastMessage?.messageId){
//                                                val messageRef =
//                                                    conversationsRef?.child(
//                                                        conversation.conversationId!!)?.child("lastMessage")?.ref
//                                                messageRef?.child("deliveredTimestamp")?.setValue(timestamp)
//                                            }
                                        }
                                    }
                                }
                            }

                            conversation?.interlocutor = userAccount
                            conversation?.unreadMessages = unreadMessageCounter

                            if (conversation != null) {
                                conversations.add(conversation)
                            }
                        }
                    }
                }
                mListener.onDataChangeReceived(conversations)
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }
        })
    }

    private fun updateMessageDeliveredTime(conversationId: String, messageId: String, timestamp: Long) {

        val messageRef =
            conversationsRef?.child(conversationId)?.child("messages")?.child(messageId)?.ref
        messageRef?.child("deliveredTimestamp")?.setValue(timestamp)
    }
}

interface ChatHistoryListener {
    fun onDataChangeReceived(conversations: List<Conversation>)
}