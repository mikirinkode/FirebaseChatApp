package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.utils.PermissionManager

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
        const val BUNDLE_CONVERSATION_TYPE = "conversationType"
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
        // TODO: merge
        // data from previous activity
        val interlocutorIdFromActivity = intent?.getStringExtra(EXTRA_INTENT_INTERLOCUTOR_ID)
        val idFromActivity = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_ID)
        val typeFromActivity = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_TYPE)
        if (interlocutorIdFromActivity != null && typeFromActivity != null) {
            setupNavigation(idFromActivity, typeFromActivity, interlocutorIdFromActivity, "AnotherActivity")
        }

        // data from notification that sent from system tray
//        val extras = intent?.extras
//        val interlocutorIdFromFCM = extras?.getString(EXTRA_INTENT_INTERLOCUTOR_ID)
//        val idFromFCM = extras?.getString(EXTRA_INTENT_CONVERSATION_ID)
//        val typeFromFCM = extras?.getString(EXTRA_INTENT_CONVERSATION_TYPE)
//        if (idFromFCM != null && typeFromFCM != null && interlocutorIdFromFCM != null) {
//            setupNavigation(idFromFCM, typeFromFCM, interlocutorIdFromFCM, null)
//        }
    }


    private fun setupNavigation(
        conversationId: String?,
        conversationType: String,
        interlocutorId: String,
        navigateFrom: String?
    ) {
        val navController = findNavController(R.id.navHostChatRoom)
        val bundle = Bundle()
        bundle.putString(BUNDLE_CONVERSATION_ID, conversationId)
        bundle.putString(BUNDLE_CONVERSATION_TYPE, conversationType)
        bundle.putString(BUNDLE_INTERLOCUTOR_ID, interlocutorId)
        bundle.putString(BUNDLE_NAVIGATE_FROM, navigateFrom)

        navController.setGraph(R.navigation.chat_room_navigation, bundle)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.CAMERA_REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (result in grantResults) {
                    Toast.makeText(this, "Camera Permission is Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}