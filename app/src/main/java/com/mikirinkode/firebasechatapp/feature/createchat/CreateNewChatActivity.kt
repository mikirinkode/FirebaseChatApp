package com.mikirinkode.firebasechatapp.feature.createchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.databinding.ActivityCreateNewChatBinding


class CreateNewChatActivity : AppCompatActivity() {

    private val binding: ActivityCreateNewChatBinding by lazy {
        ActivityCreateNewChatBinding.inflate(layoutInflater)
    }

    companion object {
        const val EXTRA_INTENT_CONVERSATION_TYPE = "intent_conversation_type"
        const val EXTRA_INTENT_CONVERSATION_ID = "intent_conversation_id"
        const val EXTRA_INTENT_PARTICIPANTS_ID = "intent_participants_id"

        const val BUNDLE_CONVERSATION_ID = "conversationId"
        const val BUNDLE_PARTICIPANTS_ID = "participantsId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // handle intent
        handleIntent()
    }

    private fun handleIntent() {
        val conversationType = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_TYPE)
        val conversationId = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_ID)
        val participants = intent.getStringArrayListExtra(EXTRA_INTENT_PARTICIPANTS_ID)

        if (conversationType != null) {
            setupNavigation(conversationType, conversationId, participants)
        }
    }

    private fun setupNavigation(conversationType: String, conversationId: String?, participants: List<String>?) {
        val navController = findNavController(R.id.navHostCreateNewChat)
                val graph = navController.navInflater.inflate(R.navigation.create_chat_navigation)

        when (conversationType) {
            ConversationType.GROUP.toString() -> {
                val participantList = if (participants.isNullOrEmpty()) null else participants.toTypedArray()

                graph.setStartDestination(R.id.selectUserFragment)
                val bundle = Bundle()
                bundle.putString(BUNDLE_CONVERSATION_ID, conversationId)
                bundle.putStringArray(BUNDLE_PARTICIPANTS_ID, participantList)
                navController.setGraph(graph, bundle)
            }
            else -> {
                graph.setStartDestination(R.id.userListFragment)
                navController.graph = graph
            }
        }
    }
}