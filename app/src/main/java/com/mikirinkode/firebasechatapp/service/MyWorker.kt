package com.mikirinkode.firebasechatapp.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

// TODO
class MyWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val database = FirebaseProvider.instance().getDatabase()
    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val pref = LocalSharedPref.instance()

    override fun doWork(): Result {

        val loggedUserId = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)?.userId
        val conversationIdList: List<String>? =
            pref?.getObjectsList(PreferenceConstant.CONVERSATION_ID_LIST, String::class.java)


        conversationIdList?.forEach { conversationId ->

            messagesRef?.child(conversationId)
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val theLatestMessage =
                            dataSnapshot.children.lastOrNull()
                                ?.getValue(ChatMessage::class.java)

                        for (snapshot in dataSnapshot.children) {
                            val chatMessage = snapshot.getValue(ChatMessage::class.java)

                            if (chatMessage != null) {
                                if (chatMessage.senderId != loggedUserId) {
                                    if (!chatMessage.beenDeliveredTo.containsKey(loggedUserId)) {
                                        val timeStamp = System.currentTimeMillis()
                                        updateMessageDeliveredTime(
                                            conversationId,
                                            timeStamp,
                                            snapshot?.key ?: ""
                                        )

                                        if (chatMessage.messageId == theLatestMessage?.messageId) {
                                            updateLastMessageDeliveredTime(
                                                conversationId,
                                                timeStamp
                                            )
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


        return Result.success()
    }


    private fun updateMessageDeliveredTime(
        conversationId: String,
        timeStamp: Long,
        messageId: String
    ) {
        val messageRef =
            messagesRef?.child(conversationId)?.child(messageId)?.ref
        messageRef?.child("deliveredTimestamp")?.setValue(timeStamp)
    }

    private fun updateLastMessageDeliveredTime(conversationId: String, timeStamp: Long) {
        val conversationRef = conversationsRef?.child(conversationId)?.child("lastMessage")?.ref
        val update = mapOf("deliveredTimestamp" to timeStamp)
        conversationRef?.updateChildren(update)
    }
}