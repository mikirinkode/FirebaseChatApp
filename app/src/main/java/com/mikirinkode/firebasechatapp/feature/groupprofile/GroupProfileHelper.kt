package com.mikirinkode.firebasechatapp.feature.groupprofile

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class GroupProfileHelper(
    private val mListener: GroupProfileListener
) {
    private val firestore = FirebaseProvider.instance().getFirestore()
    private val database = FirebaseProvider.instance().getDatabase()

    private val conversationsRef = database?.getReference("conversations")


    fun getParticipantList(participantsId: List<String>) {
        val userList = ArrayList<UserAccount>()

        firestore?.collection("users")
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
                // TODO: on fail
            }
    }

    fun getGroupData(conversationId: String) {
        val refWithQuery = conversationsRef?.child(conversationId)

        refWithQuery?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(conversationSnapshot: DataSnapshot) {
                val conversation = conversationSnapshot.getValue(Conversation::class.java)
                if (conversation != null) {
                    mListener.onReceiveGroupData(conversation)
                }
            }

            override fun onCancelled(error: DatabaseError) {
//                TODO("Not yet implemented")
            }
        })
    }
}

interface GroupProfileListener {
    fun onParticipantsDataReceived(participants: List<UserAccount>)
    fun onReceiveGroupData(conversation: Conversation)
}