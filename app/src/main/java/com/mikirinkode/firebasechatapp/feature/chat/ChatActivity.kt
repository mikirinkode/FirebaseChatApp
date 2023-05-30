package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // handle intent
        handleIntent()
    }

    private fun handleIntent() {
        val interlocutorId = intent.getStringExtra(EXTRA_INTENT_INTERLOCUTOR_ID)


        val navController = findNavController(R.id.nav_host_chat_room)

        val bundle = Bundle()

        bundle.putString(BUNDLE_INTERLOCUTOR_ID, "$interlocutorId")

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