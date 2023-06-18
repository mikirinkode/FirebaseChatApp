package com.mikirinkode.firebasechatapp.feature.selectuser

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GroupHelper(
    private val mListener: GroupParticipantListener
) {

    private val fireStore = FirebaseProvider.instance().getFirestore()
    private val database = FirebaseProvider.instance().getDatabase()


    fun addParticipantsToGroup(conversationId: String, participantsId: List<String>) {
        var conversationRef = database?.getReference("conversations")?.child(conversationId)

        CoroutineScope(Dispatchers.Main).launch {
            for (userId in participantsId){

                // update on user collection
                val userRef = fireStore?.collection("users")?.document(userId)
                userRef?.update("conversationIdList", FieldValue.arrayUnion(conversationId))

                // update on conversation collection // TODO
                conversationRef?.child("participants")?.child(userId)?.setValue(true)
            }
        }
        mListener.onSuccessAddParticipantsToGroup()
    }
}

interface GroupParticipantListener {
    fun onSuccessAddParticipantsToGroup()
}