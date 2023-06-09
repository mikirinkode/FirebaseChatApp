package com.mikirinkode.firebasechatapp.feature.main

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ChatHistoryHelper(
    private val mListener: ChatHistoryListener
) {
    private val database = FirebaseProvider.instance().getDatabase()
    private val firestore = FirebaseProvider.instance().getFirestore()
    private val pref = LocalSharedPref.instance()

    private val conversationsRef = database?.getReference("conversations")
    private val usersRef = database?.getReference("users")

    // TODO: is it possible if use fucntion from FirebaseUserHelper?
    // i think no, cause it is more complicated
    private suspend fun getUserById(userId: String): UserAccount? {
        val querySnapshot = firestore?.collection("users")
            ?.whereEqualTo("userId", userId)
            ?.get()?.await()

        val userDoc = querySnapshot?.documents?.first()
        return userDoc?.toObject(UserAccount::class.java)
    }

    fun receiveMessageHistory() {
        Log.e("ChatHistoryHelper", "receiveMessageHistory called")
        val currentUser = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)

        val conversations = mutableListOf<Conversation>()

        currentUser?.userId?.let { userId ->
            usersRef?.child(userId)?.keepSynced(true)

            usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    Log.e("ChatHistoryHelper", "onDataChange")

                    val user = userSnapshot.getValue(UserRTDB::class.java)

                    val idList = arrayListOf<String>()

                    user?.conversationIdList?.forEach { (id, _) -> idList.add(id) }

                    pref?.saveObjectsList(
                        PreferenceConstant.CONVERSATION_ID_LIST,
                        idList
                    )

                    // TODO: Check Again, kemungkinan boros memory / tidak optimal
                    user?.conversationIdList?.forEach { (conversationId, value) ->
                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
                            ?.equalTo(conversationId)

                        refWithQuery?.keepSynced(true)

                        refWithQuery?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(conversationSnapshot: DataSnapshot) {

                                for (snapshot in conversationSnapshot.children) {
                                    val conversation = snapshot.getValue(Conversation::class.java)
                                    val firstUserId = conversation?.participants?.first().toString()
                                    val secondUserId =
                                        conversation?.participants?.last().toString()

                                    val interlocutorId =
                                        if (firstUserId == userId) secondUserId else firstUserId

                                    if (interlocutorId != "null") {
                                        // Get user data by interlocutor ID
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val interlocutorUser = getUserById(interlocutorId)
                                            // Check if the interlocutorUser is not null
                                            if (interlocutorUser != null) {
                                                // Add the user data to the conversation object
                                                conversation?.interlocutor = interlocutorUser
                                                // Add the conversation object to the conversations list
                                                if (conversation != null) {
                                                    val oldConversation = conversations.find { it.conversationId == conversation.conversationId }
                                                    conversations.remove(oldConversation)
                                                    conversations.add(conversation)

                                                    mListener.onDataChangeReceived(conversations)
                                                }
                                            }
                                        }
                                    }
                                }
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