package com.mikirinkode.firebasechatapp.feature.selectuser

import android.util.Log
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GroupHelper(
    private val mListener: GroupParticipantListener
) {

    private val database = FirebaseProvider.instance().getDatabase()
    private val usersRef = database?.getReference("users")

    fun addParticipantsToGroup(conversationId: String, participantsId: List<String>) {

        Log.e("GroupHelper", "addParticipantsToGroup")
        Log.e("GroupHelper", "addParticipantsToGroup: ${conversationId}")
        Log.e("GroupHelper", "addParticipantsToGroup: ${participantsId}")
        CoroutineScope(Dispatchers.Main).launch {
            for (userId in participantsId){
                usersRef?.child(userId)?.child("conversationIdList")?.child(conversationId)
                    ?.setValue(mapOf(conversationId to true))?.await()
            }
        }
        mListener.onSuccessAddParticipantsToGroup()
    }
}

interface GroupParticipantListener {
    fun onSuccessAddParticipantsToGroup()
}