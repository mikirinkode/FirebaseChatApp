package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class ChatActivity : AppCompatActivity() {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val GALLERY_REQUEST_CODE = 2
        const val EXTRA_INTENT_INTERLOCUTOR_ID = "key_interlocutor_id"

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
        val idFromActivity = intent.getStringExtra(EXTRA_INTENT_INTERLOCUTOR_ID)
        if (idFromActivity != null) {
            setupNavigation(idFromActivity, "MainActivity")
        }

        // data from notification that sent from system tray
        val extras = intent?.extras
        val idFromFCM = extras?.getString("senderId")
        if (idFromFCM != null) {
            setupNavigation(idFromFCM, null)
        }
    }


    private fun setupNavigation(interlocutorId: String, navigateFrom: String?) {
        val navController = findNavController(R.id.nav_host_chat_room)
        val bundle = Bundle()
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