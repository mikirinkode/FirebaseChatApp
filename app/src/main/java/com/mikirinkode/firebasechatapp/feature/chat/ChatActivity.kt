package com.mikirinkode.firebasechatapp.feature.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.helper.DateHelper
import com.mikirinkode.firebasechatapp.helper.ImageHelper
import com.mikirinkode.firebasechatapp.utils.PermissionManager
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: ChatPresenter

    private var openedUserId: String? = null
    private var openedUserName: String? = null
    private lateinit var chatRoomFragment: ChatRoomFragment

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val GALLERY_REQUEST_CODE = 2
        const val EXTRA_INTENT_OPENED_USER_ID = "key_opened_id"
        const val EXTRA_INTENT_OPENED_USER_AVATAR = "key_opened_avatar"
        const val EXTRA_INTENT_OPENED_USER_NAME = "key_opened_name"


        const val BUNDLE_OPENED_USER_ID = "openedUserId"
        const val BUNDLE_OPENED_USER_NAME = "openedUserName"
        const val BUNDLE_OPENED_USER_AVATAR = "openedUserAvatar"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // handle intent
        handleIntent()
    }

    private fun handleIntent() {
        openedUserId = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_ID)
        val openedAvatar = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_AVATAR)
        openedUserName = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_NAME)


        val navController = findNavController(R.id.nav_host_chat_room)

        val bundle = Bundle()

        bundle.putString(BUNDLE_OPENED_USER_ID, "$openedUserId")
        bundle.putString(BUNDLE_OPENED_USER_NAME, "$openedUserName")
        bundle.putString(BUNDLE_OPENED_USER_AVATAR, "$openedAvatar")

        navController.setGraph(R.navigation.chat_room_navigation, bundle)
    }
}