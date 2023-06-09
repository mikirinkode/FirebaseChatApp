package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.ActivityChatGroupBinding
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class GroupChatActivity : AppCompatActivity() {
    private val binding: ActivityChatGroupBinding by lazy {
        ActivityChatGroupBinding.inflate(layoutInflater)
    }
    companion object {
        const val EXTRA_INTENT_CONVERSATION_ID = "key_conversation_id" // TODO: duplicate
        const val BUNDLE_CONVERSATION_ID = "conversationId"
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
        val idFromActivity = intent.getStringExtra(EXTRA_INTENT_CONVERSATION_ID)
        if (idFromActivity != null) {
            setupNavigation(idFromActivity, "AnotherActivity",)
        }

        // data from notification that sent from system tray
        val extras = intent?.extras
        val idFromFCM = extras?.getString(EXTRA_INTENT_CONVERSATION_ID)
        if (idFromFCM != null ) {
            setupNavigation(idFromFCM, null)
        }
    }

    private fun setupNavigation(conversationId: String?, navigateFrom: String?) {
        Log.e("GroupChat", "setupNavigation")
        Log.e("GroupChat", "conversationId: $conversationId")
        val navController = findNavController(R.id.navHostGroupChatRoom)
        val bundle = Bundle()
        bundle.putString(BUNDLE_CONVERSATION_ID, conversationId)
        bundle.putString(BUNDLE_NAVIGATE_FROM, navigateFrom)
        navController.setGraph(R.navigation.group_chat_room_navigation, bundle)
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