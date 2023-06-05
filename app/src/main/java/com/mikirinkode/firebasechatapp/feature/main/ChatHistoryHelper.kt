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

        currentUser?.uid?.let { userId ->
            usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    Log.e("ChatHistoryHelper", "onDataChange")

                    val user = userSnapshot.getValue(UserRTDB::class.java)

                    val idList = arrayListOf<String>()

                    user?.conversationIdList?.forEach { (id, _) -> idList.add(id) }

                    pref?.saveObjectsList(
                        DataConstant.CONVERSATION_ID_LIST,
                        idList
                    )

                    // TODO: Check Again, kemungkinan boros memory / tidak optimal
                    user?.conversationIdList?.forEach { (conversationId, value) ->
                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
                            ?.equalTo(conversationId)

                        refWithQuery?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(conversationSnapshot: DataSnapshot) {

                                for (snapshot in conversationSnapshot.children) {
                                    val conversation = snapshot.getValue(Conversation::class.java)
                                    val firstUserId = conversation?.participants?.first().toString()
                                    val secondUserId =
                                        conversation?.participants?.last().toString()

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
    }
}

interface ChatHistoryListener {
    fun onDataChangeReceived(conversations: List<Conversation>)
}