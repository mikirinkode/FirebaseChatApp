package com.mikirinkode.firebasechatapp.feature.chat.chatroom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.commonhelper.PermissionHelper
import com.mikirinkode.firebasechatapp.constants.ConversationType

class ConversationActivity : AppCompatActivity() {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val GALLERY_REQUEST_CODE = 2
        const val EXTRA_INTENT_INTERLOCUTOR_ID = "intent_interlocutor_id"
        const val EXTRA_INTENT_CONVERSATION_ID = "intent_conversation_id"
        const val EXTRA_INTENT_CONVERSATION_TYPE = "intent_conversation_type"

        const val BUNDLE_CONVERSATION_ID = "conversationId"
        const val BUNDLE_INTERLOCUTOR_ID = "interlocutorId"
        const val BUNDLE_NAVIGATE_FROM = "navigateFrom"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // handle intent
        handleIntent()
    }

    private fun handleIntent() {
        // data from previous activity
        val interlocutorId = intent?.getStringExtra(EXTRA_INTENT_INTERLOCUTOR_ID)
        val conversationId = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_ID)
        val conversationType = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_TYPE)

        if (conversationType != null) {
            setupNavigation(conversationId, conversationType, interlocutorId, "AnotherActivity")
        }
    }


    private fun setupNavigation(
        conversationId: String?,
        conversationType: String,
        interlocutorId: String?,
        navigateFrom: String?
    ) {
        val navController = findNavController(R.id.navHostChatRoom)

        Log.e("ConversationActivity", "setup navigation")
        Log.e("ConversationActivity", "conversation id: ${conversationId}")
        Log.e("ConversationActivity", "conversation type: ${conversationType}")
        Log.e("ConversationActivity", "interlocutor id: ${interlocutorId}")

        when (conversationType) {
            ConversationType.PERSONAL.toString() -> {
                if (conversationId != null && interlocutorId != null) {
                    val bundle = Bundle()
                    bundle.putString(BUNDLE_CONVERSATION_ID, conversationId)
                    bundle.putString(BUNDLE_INTERLOCUTOR_ID, interlocutorId)
                    bundle.putString(BUNDLE_NAVIGATE_FROM, navigateFrom)
                    navController.setGraph(R.navigation.personal_chat_room_navigation, bundle)
                }
            }
            ConversationType.GROUP.toString() -> {
                val bundle = Bundle()
                bundle.putString(BUNDLE_CONVERSATION_ID, conversationId)
                bundle.putString(BUNDLE_NAVIGATE_FROM, navigateFrom)
                navController.setGraph(R.navigation.group_chat_room_navigation, bundle)
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.CAMERA_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    Toast.makeText(this, "Camera Permission is Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}