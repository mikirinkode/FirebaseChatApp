package com.mikirinkode.firebasechatapp.feature.chat.chatroom.group

import android.net.Uri
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.constants.Constants
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.onesignal.OneSignal
import org.json.JSONArray
import org.json.JSONObject

class GroupConversationHelper(
    private val mListener: GroupConversationListener,
    private val conversationId: String
) {
    private val fireStore = FirebaseProvider.instance().getFirestore()
    private val database = FirebaseProvider.instance().getDatabase()
    private val storage = FirebaseProvider.instance().getStorage()

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")

    private val pref = LocalSharedPref.instance()
    private val loggedUser = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    private val participantIdList = ArrayList<String>()

    private val receiveListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val messages = mutableListOf<ChatMessage>()
            val theLatestMessage =
                dataSnapshot.children.lastOrNull()?.getValue(ChatMessage::class.java)

            for (snapshot in dataSnapshot.children) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (loggedUser?.userId != null && chatMessage.senderId != loggedUser.userId) {
                        if (!chatMessage.beenReadBy.containsKey(loggedUser.userId!!)) {

                            val timeStamp = System.currentTimeMillis()
                            updateMessageReadTime(
                                timeStamp,
                                loggedUser.userId!!,
                                snapshot?.key ?: ""
                            )

                            if (chatMessage.messageId == theLatestMessage?.messageId) {
                                updateLastMessageReadTime(
                                    loggedUser.userId!!, timeStamp
                                )
                            }
                        }
                    }
                    messages.add(chatMessage)
                }
            }
            val sortedMessages = messages.sortedBy { it.sendTimestamp }
            mListener.onMessageReceived(sortedMessages)
        }

        override fun onCancelled(error: DatabaseError) {
            // TODO: UNIMPLEMENTED
        }
    }


    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        receiverDeviceTokenList: List<String>
    ) {
        val timeStamp = System.currentTimeMillis()
        val newMessageKey = conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
        if (newMessageKey != null) {
            val chatMessage = ChatMessage(
                messageId = newMessageKey,
                message = message,
                sendTimestamp = timeStamp,
                type = MessageType.TEXT.toString(),
                senderId = senderId,
                senderName = senderName
            )

            val updateLastMessage = mapOf(
                "lastMessage" to chatMessage
            )

            // update total unread messages
            updateTotalUnreadMessages()

            // push last message
            conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

            // push message
            messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)

            // post notification
            postNotification(senderName, message, receiverDeviceTokenList)

            // reset total unread messages
            resetTotalUnreadMessage()
        }

    }

    fun sendMessage(
        message: String,
        senderId: String,
        senderName: String,
        file: Uri,
        path: String,
        receiverDeviceTokenList: List<String>
    ) {
        val sRef: StorageReference? =
            storage?.reference?.child("conversations/${conversationId}")?.child(path)

        sRef?.putFile(file)?.addOnSuccessListener {
            it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val timeStamp = System.currentTimeMillis()

                val newMessageKey =
                    conversationsRef?.child(conversationId)?.child("messages")?.push()?.key
                if (newMessageKey != null) {
                    val chatMessage = ChatMessage(
                        messageId = newMessageKey,
                        message = message,
                        imageUrl = uri.toString(),
                        sendTimestamp = timeStamp,
                        type = MessageType.IMAGE.toString(),
                        senderId = senderId,
                        senderName = senderName
                    )


                    val updateLastMessage = mapOf(
                        "lastMessage" to chatMessage
                    )

                    // update total unread messages
                    updateTotalUnreadMessages()

                    // push last message
                    conversationsRef?.child(conversationId)?.updateChildren(updateLastMessage)

                    // push message
                    messagesRef?.child(conversationId)?.child(newMessageKey)?.setValue(chatMessage)

                    // post notification
                    postNotification(senderName, message, receiverDeviceTokenList)

                    // reset total unread messages
                    resetTotalUnreadMessage()
                }
            }
        }?.addOnProgressListener { taskSnapshot ->
            // Update the progress bar as the upload progresses
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            mListener.showUploadImageProgress(progress)
        }
    }

    private fun postNotification(
        senderName: String,
        message: String,
        receiverDeviceTokenList: List<String>
    ) {
        val receivers = JSONArray(receiverDeviceTokenList)
        val customData = JSONObject().apply {
            put("conversationId", conversationId)
            put("conversationType", ConversationType.GROUP.toString())
        }
        val notificationJson = JSONObject().apply {
            put("app_id", Constants.ONE_SIGNAL_APP_ID)
            put("include_player_ids", receivers)
            put("contents", JSONObject().put("en", message))
            put("headings", JSONObject().put("en", senderName))
            put("data", customData)
        }

        OneSignal.postNotification(
            notificationJson,
            object : OneSignal.PostNotificationResponseHandler {
                override fun onSuccess(response: JSONObject?) {
                    // Notification sent successfully
                }

                override fun onFailure(response: JSONObject?) {
                    // Failed to send notification
                }
            })
    }

    fun getParticipantList(participantsId: List<String>) {
        val userList = ArrayList<UserAccount>()

        if (participantsId.isNotEmpty()){
            fireStore?.collection("users")
                ?.whereIn("userId", participantsId)
                ?.get()
                ?.addOnSuccessListener { documentList ->
                    for (document in documentList) {
                        if (document != null) {
                            val userAccount: UserAccount = document.toObject()
                            userList.add(userAccount)
                        }
                    }
                    mListener.onParticipantsDataReceived(userList)
                }
                ?.addOnFailureListener {
                    // TODO: UNIMPLEMENTED
                }
        }
    }

    fun receiveMessages() {
        val ref = conversationId?.let { messagesRef?.child(it) }
        ref?.keepSynced(true)

        ref?.addValueEventListener(receiveListener)
    }

    fun getConversationById(conversationId: String) {
        val refWithQuery = conversationsRef?.child(conversationId)

        refWithQuery?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(conversationSnapshot: DataSnapshot) {
                val conversation = conversationSnapshot.getValue(Conversation::class.java)
                if (conversation != null) {
                    participantIdList.clear()
                    participantIdList.addAll(conversation.participants.keys.toList())
                    getParticipantList(conversation.participants.keys.toList()) // TODO: is this efficient?
                    mListener.onConversationDataReceived(conversation)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: UNIMPLEMENTED
            }
        })
    }

    fun deactivateListener() {
        messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
    }


    private fun updateMessageReadTime(timeStamp: Long, userId: String, messageId: String) {
        messagesRef?.child(conversationId)?.child(messageId)?.child("beenReadBy")?.child(userId)
            ?.setValue(timeStamp)
    }

    private fun updateLastMessageReadTime(userId: String, timeStamp: Long) {
        conversationsRef?.child(conversationId)?.child("lastMessage")?.child("beenReadBy")
            ?.child(userId)?.setValue(timeStamp)
    }

    // Reset the total unread message
    fun resetTotalUnreadMessage() {
        loggedUser?.userId?.let {
            conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")?.child(
                it
            )?.setValue(0)
        }
    }

    private fun updateTotalUnreadMessages() {
        if (participantIdList.isEmpty()) {
            conversationsRef?.child(conversationId)?.child("participants")?.get()
                ?.addOnSuccessListener { participants ->
                    participants.children.forEach { participant ->
                        val userId = participant.key
                        if (userId != loggedUser?.userId) {
                            if (userId != null) {
                                conversationsRef.child(conversationId)
                                    .child("unreadMessageEachParticipant")
                                    .child(userId).setValue(ServerValue.increment(1))
                            }
                        }
                    }
                }
        }
        participantIdList.forEach { userId ->
            if (userId != loggedUser?.userId) {
                conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")
                    ?.child(userId)?.setValue(ServerValue.increment(1))
            }
        }
    }
}

interface GroupConversationListener {
    fun onMessageReceived(messages: List<ChatMessage>)
    fun showUploadImageProgress(progress: Int)
    fun onConversationDataReceived(conversation: Conversation)
    fun onParticipantsDataReceived(participants: List<UserAccount>)
}