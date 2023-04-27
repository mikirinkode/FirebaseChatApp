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
                    val userIds = snapshot.key?.split("-")
                    val firstUserId = userIds?.first()
                    val secondUserId = userIds?.last()

                    if (firstUserId == currentUser?.uid) {
                        val conversation = snapshot.getValue(Conversation::class.java)
                        Log.e("ChatHistoryHelper", "total messages : ${conversation?.messages?.size}")
                        runBlocking {
                            val documents = secondUserId?.let { getUserById(it) }
                            val userAccount: UserAccount? = documents?.first()?.toObject()
                            conversation?.interlocutor = userAccount
                            if (conversation != null) {
                                conversations.add(conversation)
                            }

                            conversation?.messages?.forEach { (key, message) ->
                                if (message.deliveredTimestamp == 0L && message.receiverId == currentUser?.uid){
                                    if (conversation.conversationId != null){
                                        updateMessageDeliveredTime(conversation.conversationId!!, message.messageId)
                                    }
                                }
                            }
                        }
                    } else if (secondUserId == currentUser?.uid) {
                        val conversation = snapshot.getValue(Conversation::class.java)
                        Log.e("ChatHistoryHelper", "total messages : ${conversation?.messages?.size}")
                        runBlocking {
                            val documents = firstUserId?.let { getUserById(it) }
                            val userAccount: UserAccount? = documents?.first()?.toObject()
                            conversation?.interlocutor = userAccount
                            if (conversation != null) {
                                conversations.add(conversation)
                            }
                            conversation?.messages?.forEach { (key, message) ->
                                if (message.deliveredTimestamp == 0L && message.receiverId == currentUser?.uid){
                                    if (conversation.conversationId != null){
                                        updateMessageDeliveredTime(conversation.conversationId!!, message.messageId)
                                    }
                                }
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

    private fun updateMessageDeliveredTime(conversationId: String, messageId: String) {
        val timeStamp = System.currentTimeMillis()

        val messageRef =
            conversationsRef?.child(conversationId)?.child("messages")?.child(messageId)?.ref
        messageRef?.child("deliveredTimestamp")?.setValue(timeStamp)
    }
}

interface ChatHistoryListener {
    fun onDataChangeReceived(conversations: List<Conversation>)
}