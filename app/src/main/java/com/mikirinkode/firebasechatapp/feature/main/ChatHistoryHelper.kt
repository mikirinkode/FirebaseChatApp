package com.mikirinkode.firebasechatapp.feature.main

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.local.pref.DataConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MainHelper(
    private val mListener: ChatHistoryListener
) {
    private val auth = FirebaseProvider.instance().getFirebaseAuth()
    private val database = FirebaseProvider.instance().getDatabase()
    private val storage = FirebaseProvider.instance().getStorage()
    private val firestore = FirebaseProvider.instance().getFirestore()
    private val pref = LocalSharedPref.instance()

    private val conversationsRef = database?.getReference("conversations")
    private val usersRef = database?.getReference("users")

    // TODO: move to FirebaseUserHelper
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

        Log.e("ChatHistoryHelper", "database: ${database}")
        Log.e("ChatHistoryHelper", "ref: ${usersRef}")
        Log.e("ChatHistoryHelper", "current user: ${currentUser}")
        Log.e("ChatHistoryHelper", "current user uid: ${currentUser?.uid}")

//        database?.getReference("users")?.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//        Log.e("ChatHistoryHelper", "onDataChange called")
//        Log.e("ChatHistoryHelper", "onDataChange called")
//        Log.e("ChatHistoryHelper", "onDataChange called")
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
////                TODO("Not yet implemented")
//            }
//
//        }
//        )

        currentUser?.uid?.let { userId ->
            usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    Log.e("ChatHistoryHelper", "onDataChange")
                    Log.e(
                        "ChatHistoryHelper",
                        "userSnapshot: ${userSnapshot}"
                    )
                    val user = userSnapshot.getValue(UserRTDB::class.java)
                    Log.e(
                        "ChatHistoryHelper",
                        "conversations id list: ${user?.conversationIdList}"
                    )

                    val idList = arrayListOf<String>()

                    user?.conversationIdList?.forEach { (id, _) -> idList.add(id) }

                    pref?.saveObjectsList(
                        DataConstant.CONVERSATION_ID_LIST,
                        idList
                    )


//                    conversations.clear()
                    user?.conversationIdList?.forEach { conversationId, value ->
                        Log.e("ChatHistoryHelper", "conversations id ${conversationId}")
                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
                            ?.equalTo(conversationId) // todo

//                        conversationsRef?.child(conversationId)?.addChildEventListener(object: ChildEventListener {
//                            override fun onChildAdded(
//                                snapshot: DataSnapshot,
//                                previousChildName: String?
//                            ) {}
//
//                            override fun onChildChanged(
//                                snapshot: DataSnapshot,
//                                previousChildName: String?
//                            ) {
//                                Log.e("CHH", "onChildChanged: ${snapshot}")
//                                                                    val conversation = snapshot.getValue(Conversation::class.java)
//                                    val firstUserId = conversation?.participants?.first().toString()
//                                    val secondUserId =
//                                        conversation?.participants?.last().toString()
//
//                                    Log.e("ChatHistoryHelper", "firstUserId: ${firstUserId}, secondUserId: ${secondUserId}")
//
//                                    val interlocutorId =
//                                        if (firstUserId == currentUser.uid) secondUserId else firstUserId
//
//                                    runBlocking {
//
//                                        if (interlocutorId != "null") {
//                                            val userDoc = getUserById(interlocutorId)
//                                            val userAccount: UserAccount? =
//                                                userDoc?.first()?.toObject()
//                                            var unreadMessageCounter = 0
//
//                                            conversation?.interlocutor = userAccount
//                                            conversation?.unreadMessages = unreadMessageCounter
//                                        }
//
//                                        if (conversation != null) {
////                                            conversations.add(conversation)
////                                            conversations.contains()
//                                        }
//                                    }
//
//                            }
//
//                            override fun onChildRemoved(snapshot: DataSnapshot) {}
//
//                            override fun onChildMoved(
//                                snapshot: DataSnapshot,
//                                previousChildName: String?
//                            ) {}
//
//                            override fun onCancelled(error: DatabaseError) {
//                                Log.e("ChatHistoryHelper", "onCancelled: ${error.message}")
//                            }
//
//                        })

                        refWithQuery?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(conversationSnapshot: DataSnapshot) {
                                Log.e("ChatHistoryHelper", "ref with query")
                                Log.e(
                                    "ChatHistoryHelper",
                                    "conversationSnapshot: ${conversationSnapshot}"
                                )

                                for (snapshot in conversationSnapshot.children) {
                                    val conversation = snapshot.getValue(Conversation::class.java)
                                    val firstUserId = conversation?.participants?.first().toString()
                                    val secondUserId =
                                        conversation?.participants?.last().toString()

                                    Log.e(
                                        "ChatHistoryHelper",
                                        "firstUserId: ${firstUserId}, secondUserId: ${secondUserId}"
                                    )

                                    val interlocutorId =
                                        if (firstUserId == currentUser.uid) secondUserId else firstUserId

                                    runBlocking {

                                        if (interlocutorId != "null") {
                                            val userDoc = getUserById(interlocutorId)
                                            val userAccount: UserAccount? =
                                                userDoc?.first()?.toObject()

                                            conversation?.interlocutor = userAccount
                                        }

                                        if (conversation != null) {
                                            val index: Int? =
                                                conversations.indexOfFirst { it.conversationId == conversation.conversationId }
                                            if (index != null && index >= 0) {
                                                conversations[index] = conversation
                                            } else {
                                                conversations.add(conversation)
                                            }
                                        }
                                    }
                                }

                                mListener.onDataChangeReceived(conversations)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ChatHistoryHelper", "onCancelled : ${error.message}")
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatHistoryHelper", "onCancelled : ${error.message}")
                }
            })
        }

//        conversationsRef?.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.e("ChatHistoryHelper", "receiveMessageHistory listener on data change")
//                conversations.clear()
//                for (snapshot in dataSnapshot.children) {
//                    val conversationId = snapshot.key?.split("-")
//
//                    if (conversationId?.contains(currentUser?.uid) == true) {
//                        val conversation = snapshot.getValue(Conversation::class.java)
//                        val firstUserId = conversationId.first()
//                        val secondUserId = conversationId.last()
//
//                        val interlocutorId = if (firstUserId == currentUser?.uid) secondUserId else firstUserId
//
//                        runBlocking {
//
//                            val userDoc = getUserById(interlocutorId)
//                            val userAccount: UserAccount? = userDoc?.first()?.toObject()
//                            var unreadMessageCounter = 0
//
////                            conversation?.messages?.forEach { (key, message) ->
////                                if (message.receiverId == currentUser?.uid) {
////                                    if (!message.beenRead) {
////                                        unreadMessageCounter = unreadMessageCounter.plus(1)
////                                    }
////                                    if (message.deliveredTimestamp == 0L) {
////                                        val timestamp = System.currentTimeMillis()
////
////                                        if (conversation.conversationId != null) {
////                                            updateMessageDeliveredTime(
////                                                conversation.conversationId!!,
////                                                message.messageId,
////                                                timestamp
////                                            )
////
//////                                            if (message.messageId == conversation.lastMessage?.messageId){
//////                                                val messageRef =
//////                                                    conversationsRef?.child(
//////                                                        conversation.conversationId!!)?.child("lastMessage")?.ref
//////                                                messageRef?.child("deliveredTimestamp")?.setValue(timestamp)
//////                                            }
////                                        }
////                                    }
////                                }
////                            }
//
//                            conversation?.interlocutor = userAccount
//                            conversation?.unreadMessages = unreadMessageCounter
//
//                            if (conversation != null) {
//                                conversations.add(conversation)
//                            }
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

    private fun updateMessageDeliveredTime(
        conversationId: String,
        messageId: String,
        timestamp: Long
    ) {

        val messageRef =
            conversationsRef?.child(conversationId)?.child("messages")?.child(messageId)?.ref
        messageRef?.child("deliveredTimestamp")?.setValue(timestamp)
    }
}

interface ChatHistoryListener {
    fun onDataChangeReceived(conversations: List<Conversation>)
}