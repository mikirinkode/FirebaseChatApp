package com.mikirinkode.firebasechatapp.feature.main

import android.util.Log
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

    private val chatMessagesRef = database?.getReference("chatMessages")

    private suspend fun getUserById(userId: String): MutableList<DocumentSnapshot>? {
        val querySnapshot = firestore?.collection("users")
            ?.whereEqualTo("userId", userId)
            ?.get()?.await()

        return querySnapshot?.documents
    }

    // todo: last message not auto update
    // try to change the listener or change to realtime database
    fun receiveMessageHistory() {
        val currentUser = auth?.currentUser

        val conversations = mutableListOf<Conversation>()

        firestore?.collection("conversations")
            ?.whereArrayContains("userIdList", currentUser?.uid.toString())
            ?.get()
            ?.addOnSuccessListener { documentList ->
                Log.e("MainHelper", "uid: ${currentUser?.uid.toString()}")
                Log.e("MainHelper", "size: ${documentList.size()}")
                for (document in documentList) {
                    val conversation: Conversation = document.toObject()

                    // get interlocutor object
                    for (userId in conversation.userIdList) {
                        if (userId != currentUser?.uid) {
                            runBlocking {
                                val documents = getUserById(userId)
                                if (documents != null) {
                                    for (doc in documents) {
                                        val userAccount: UserAccount? = doc.toObject()

                                        Log.e("MainHelper", "user ${userAccount?.name}")
                                        conversation.interlocutor = userAccount
                                        conversations.add(conversation)
                                    }
                                }
                            }
                        }
                    }
                    mListener.onDataChangeReceived(conversations)
                }
            }


//        chatMessagesRef?.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (snapshot in dataSnapshot.children) {
//                    val userIds = snapshot.key?.split("-")
//                    val firstUserId = userIds?.first()
//                    val secondUserId = userIds?.last()
//                    if (firstUserId == currentUser?.uid) {
//                        val conversation = snapshot.getValue(Conversation::class.java)
//                        val userAccount = secondUserId?.let { getUserById(it) }
//                        conversation?.interlocutors = userAccount
//
//                        if (conversation != null) {
//                            conversations.add(conversation)
//                        }
//                    } else if (secondUserId == currentUser?.uid) {
//                        val conversation = snapshot.getValue(Conversation::class.java)
//                        val userAccount = firstUserId?.let { getUserById(it) }
//
//                        conversation?.interlocutors = userAccount
//                        if (conversation != null) {
//                            conversations.add(conversation)
//                        }
//                    }
//                }
//                mListener.onDataChangeReceived(conversations)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
////                TODO("Not yet implemented")
//            }
//        })
    }
}

interface ChatHistoryListener {
    fun onDataChangeReceived(conversations: List<Conversation>)
}