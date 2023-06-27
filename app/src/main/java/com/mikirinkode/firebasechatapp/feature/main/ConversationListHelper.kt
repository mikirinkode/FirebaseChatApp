package com.mikirinkode.firebasechatapp.feature.main

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ConversationListHelper(
    private val mListener: ChatHistoryListener
) {
    private val database = FirebaseProvider.instance().getDatabase()
    private val firestore = FirebaseProvider.instance().getFirestore()
    private val pref = LocalSharedPref.instance()

    private val conversationsRef = database?.getReference("conversations")

    private suspend fun getUserById(userId: String): UserAccount? {
        val querySnapshot = firestore?.collection("users")
            ?.document( userId)
            ?.get()?.await()

        val userDoc = querySnapshot
        return userDoc?.toObject(UserAccount::class.java)
    }

    fun receiveMessageHistory() {
        val currentUser = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)

        val conversations = mutableListOf<Conversation>()

        currentUser?.userId?.let { userId ->
            val userRef = firestore?.collection("users")?.document(userId)
            userRef
                ?.addSnapshotListener { document, error ->
                    val user: UserAccount? = document?.toObject()

                    val idList = arrayListOf<String>()

                    user?.conversationIdList?.forEach { id -> idList.add(id) }

                    if (idList.isEmpty()) {
                        mListener.onEmptyConversation()
                    }

                    pref?.saveObjectsList(
                        PreferenceConstant.CONVERSATION_ID_LIST,
                        idList
                    )

                    user?.conversationIdList?.forEach { conversationId ->
                        val refWithQuery = conversationsRef?.orderByChild("conversationId")
                            ?.equalTo(conversationId)

                        refWithQuery?.keepSynced(true)

                        refWithQuery?.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(conversationSnapshot: DataSnapshot) {

                                for (snapshot in conversationSnapshot.children) {
                                    val conversation = snapshot.getValue(Conversation::class.java)
                                    val firstUserId = conversation?.participants?.keys?.first().toString()
                                    val secondUserId =
                                        conversation?.participants?.keys?.last().toString()
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
                                                    val oldConversation =
                                                        conversations.find { it.conversationId == conversation.conversationId }
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
                            }
                        })
                    }
                }
        }
    }
}

interface ChatHistoryListener {
    fun onDataChangeReceived(conversations: List<Conversation>)

    fun onEmptyConversation()
}