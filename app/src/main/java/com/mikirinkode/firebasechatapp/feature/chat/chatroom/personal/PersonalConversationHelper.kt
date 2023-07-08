package com.mikirinkode.firebasechatapp.feature.chat.chatroom.personal

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.StorageReference
import com.mikirinkode.firebasechatapp.constants.Constants
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserStatus
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.onesignal.OneSignal
import org.json.JSONArray
import org.json.JSONObject

class PersonalConversationHelper(
    private val conversationId: String,
    private val interlocutorId: String,
    private val mListener: PersonalConversationListener,
) {
    private val fireStore = FirebaseProvider.instance().getFirestore()
    private val database = FirebaseProvider.instance().getDatabase()
    private val storage = FirebaseProvider.instance().getStorage()

    private val conversationsRef = database?.getReference("conversations")
    private val messagesRef = database?.getReference("messages")
    private val usersRef = database?.getReference("users")

    private val pref = LocalSharedPref.instance()
    private val loggedUser = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)


    // TODO: DUPLICATE LIKE IN GROUP HELPER
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
            mListener.onMessagesReceived(sortedMessages)
        }

        override fun onCancelled(error: DatabaseError) {
            // TODO: UNIMPLEMENTED
        }
    }

    fun receiveMessage() {
        val ref = conversationId.let { messagesRef?.child(it) }
        ref?.addValueEventListener(receiveListener)
    }

    fun deactivateListener() {
        messagesRef?.child(conversationId)?.removeEventListener(receiveListener)
    }

    fun createPersonaChatRoom(userId: String, anotherUserId: String) {
        // add conversation id to user firestore collection
        val userRef = fireStore?.collection("users")?.document(userId)
        userRef?.update("conversationIdList", FieldValue.arrayUnion(conversationId))
        val anotherUserRef = fireStore?.collection("users")?.document(anotherUserId)
        anotherUserRef?.update("conversationIdList", FieldValue.arrayUnion(conversationId))


        // create conversation object on realtime database
        val timeStamp = System.currentTimeMillis()
        val participants = mapOf(
            userId to mapOf(
                "joinedAt" to timeStamp
            ),
            anotherUserId to mapOf(
                "joinedAt" to timeStamp
            ),
        )
        val initialConversation = mapOf(
            "conversationId" to conversationId,
            "participants" to participants,
            "conversationType" to "PERSONAL",
            "createdAt" to timeStamp
        )
        conversationsRef?.child(conversationId)?.updateChildren(initialConversation)

        val currentUser = pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
        val newConversationList = ArrayList<String>()
        currentUser?.conversationIdList?.let { newConversationList.addAll(it) }
        newConversationList.add(conversationId)
        val newUserData = UserAccount(
            userId = currentUser?.userId,
            email = currentUser?.email,
            name = currentUser?.name,
            avatarUrl = currentUser?.avatarUrl,
            createdAt = currentUser?.createdAt,
            lastLoginAt = currentUser?.lastLoginAt,
            updatedAt = currentUser?.updatedAt,
            conversationIdList = newConversationList
        )

        pref?.saveObject(PreferenceConstant.USER, newUserData)
    }

    // TODO: DUPLICATE LIKE IN GROUP HELPER
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

    // TODO: DUPLICATE LIKE IN GROUP HELPER
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


    // Reset the total unread message
    fun resetTotalUnreadMessage() {
        loggedUser?.userId?.let {
            conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")?.child(
                it
            )?.setValue(0)
        }
    }

    private fun updateTotalUnreadMessages() {
        conversationsRef?.child(conversationId)?.child("unreadMessageEachParticipant")
            ?.child(interlocutorId)?.setValue(ServerValue.increment(1))
    }

    // TODO: DUPLICATE LIKE IN GROUP HELPER
    private fun postNotification(
        senderName: String,
        message: String,
        receiverDeviceTokenList: List<String>
    ) {
        val receivers = JSONArray(receiverDeviceTokenList)
        val customData = JSONObject().apply {
            put("conversationId", conversationId)
            put("conversationType", ConversationType.PERSONAL.toString())
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


    // TODO: DUPLICATE LIKE IN GROUP HELPER
    private fun updateMessageReadTime(timeStamp: Long, userId: String, messageId: String) {
        messagesRef?.child(conversationId)?.child(messageId)?.child("beenReadBy")?.child(userId)
            ?.setValue(timeStamp)
    }

    // TODO: DUPLICATE LIKE IN GROUP HELPER
    private fun updateLastMessageReadTime(userId: String, timeStamp: Long) {
        conversationsRef?.child(conversationId)?.child("lastMessage")?.child("beenReadBy")
            ?.child(userId)?.setValue(timeStamp)
    }

    fun getUserById(userId: String) {
        Log.e("PersonalConversationHelper", "getUserById")
        Log.e("PersonalConversationHelper", "user id: ${userId}")
        fireStore?.collection("users")
            ?.document(userId)
            ?.get()
            ?.addOnSuccessListener { document ->
                val user: UserAccount? = document.toObject()
                if (user != null) {
                    mListener.onInterlocutorDataReceived(user)
                }
            }
    }

    fun getUserStatus(userId: String) {
        usersRef?.child(userId)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(UserStatus::class.java)
                if (status != null) {
                    mListener?.onUserStatusReceived(status)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}