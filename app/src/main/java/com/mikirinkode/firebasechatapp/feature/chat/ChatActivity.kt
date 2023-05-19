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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.utils.PermissionManager
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity(), ChatView, ChatAdapter.ChatClickListener {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val chatAdapter: ChatAdapter by lazy {
        ChatAdapter()
    }

    private lateinit var presenter: ChatPresenter
    private var openedUserId: String? = null
    private var openedUserName: String? = null

    private var capturedImage: Uri? = null
//    private var photoUri: Uri? = null

    private var currentMessageType = MessageType.TEXT

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val GALLERY_REQUEST_CODE = 2
        const val EXTRA_INTENT_OPENED_USER_ID = "key_opened_id"
        const val EXTRA_INTENT_OPENED_USER_AVATAR = "key_opened_avatar"
        const val EXTRA_INTENT_OPENED_USER_NAME = "key_opened_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        handleIntent()
        setupPresenter()
        initView()
        observeMessage()
        onActionClicked()
    }

    override fun onResume() {
        super.onResume()
        if (openedUserId != null) {
            presenter.getUserOnlineStatus(openedUserId!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun handleIntent() {
        openedUserId = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_ID)
        val openedAvatar = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_AVATAR)
        openedUserName = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_NAME)

        setupReceiverProfile(openedUserName, openedAvatar)
    }


    private fun initView() {
        binding.apply {

            val userId = pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
            rvMessages.layoutManager = LinearLayoutManager(this@ChatActivity)
            rvMessages.adapter = chatAdapter
            chatAdapter.chatClickListener = this@ChatActivity

            if (userId != null) {
                chatAdapter.setLoggedUserId(userId)
            }
        }
    }

    private fun observeMessage() {
        val userId = pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
        if (userId != null && openedUserId != null) {
            presenter.receiveMessage(loggedUserId = userId, openedUserId = openedUserId!!)
        }
    }

    private fun setupReceiverProfile(openedName: String?, openedAvatar: String?) {
        binding.apply {
            tvName.text = openedName
            if (openedAvatar != null && openedAvatar != "") {
                Glide.with(this@ChatActivity)
                    .load(openedAvatar)
                    .into(ivUserAvatar)
            }
        }
    }

    private fun setupPresenter() {
        presenter = ChatPresenter()
        presenter.attachView(this)
        if (openedUserId != null) {
            presenter.getUserOnlineStatus(openedUserId!!)
        }
    }

    // Open Camera
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create a file to save the image
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Handle the error
                null
            }

            // Continue only if the file was successfully created
            photoFile?.let {
                // Get the content URI for the file using FileProvider
                capturedImage = FileProvider.getUriForFile(this, "com.mikirinkode.firebasechatapp.fileprovider", it)

                // Set the output file URI for the camera intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImage)

                // Grant permission to the camera app to write to the URI
                val cameraActivity = takePictureIntent.resolveActivity(packageManager)
                val cameraPermission = cameraActivity?.let {
                    packageManager.checkPermission(
                        android.Manifest.permission.CAMERA,
                        it.packageName
                    )
                }
                if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                    // Launch the camera intent
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE) // TODO: remove
        }
    }
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
    private fun showOnLongChatClickDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Choose Action")
            .setMessage("This is my dialog.")
            .setPositiveButton("OK") { dialog, which ->
                // Handle OK button click
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Handle Cancel button click
            }
            .show()
    }

    override fun updateMessages(messages: List<ChatMessage>) {
        chatAdapter.setData(messages)
        if (messages.isNotEmpty()) {
            binding.rvMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun updateReceiverOnlineStatus(status: UserRTDB) {

        val timestamp = Timestamp(status.lastOnlineTimestamp)
        val date = Date(timestamp.time)
        val dateFormat = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        binding.tvUserStatus.text = "Last Online at $formattedDate"
//        if (status.online){
//            binding.tvUserStatus.text = "Online"
//        } else {
//        }
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    override fun onLongClick(chat: ChatMessage) {
        showOnLongChatClickDialog()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun onActionClicked() {
        binding.apply {

            btnBack.setOnClickListener {
                onBackPressed()
                finish()
            }

            btnAddExtras.setOnClickListener {
                if (layoutSelectExtras.visibility == View.GONE) {
                    layoutSelectExtras.visibility = View.VISIBLE
                } else {
                    layoutSelectExtras.visibility = View.GONE
                }
            }

            btnSend.setOnClickListener {
                // Get a reference to the InputMethodManager
                val imm =
                    this@ChatActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                // Check if the keyboard is currently open
                if (imm.isAcceptingText) {
                    // If the keyboard is open, hide it
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
                }

                val message = etMessage.text.toString().trim()
                if (message.isNotBlank()) {
                    val senderId =
                        pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
                    val senderName =
                        pref?.getObject(DataConstant.USER, UserAccount::class.java)?.name

                    if (senderId != null && openedUserId != null) {
                        etMessage.setText("")
                        when (currentMessageType) {
                            MessageType.TEXT -> {
                                presenter.sendMessage(
                                    message,
                                    senderId,
                                    openedUserId!!,
                                    senderName!!,
                                    openedUserName!!
                                )
                            }
                            MessageType.IMAGE -> {
                                if (capturedImage != null) {
                                    val imageExtension = MimeTypeMap.getSingleton()
                                        .getExtensionFromMimeType(
                                            contentResolver.getType(
                                                capturedImage!!
                                            )
                                        )

                                    val path =
                                        "messages/images-" + System.currentTimeMillis() + "." + imageExtension
                                    presenter.sendMessage(
                                        message,
                                        senderId,
                                        openedUserId!!,
                                        senderName!!,
                                        openedUserName!!,
                                        capturedImage!!,
                                        path
                                    )
                                    binding.layoutSelectExtras.visibility = View.GONE
                                    binding.layoutSelectedImage.visibility = View.GONE
                                    capturedImage = null
                                }
                            }
                            MessageType.VIDEO -> {}
                            MessageType.AUDIO -> {}
                        }
                    }
                }
            }

            btnCamera.setOnClickListener {
                if (PermissionManager.isCameraPermissionGranted(this@ChatActivity)) {
                    // Open Camera
                    dispatchTakePictureIntent()
                } else {
                    PermissionManager.requestCameraPermission(this@ChatActivity)
                }
            }

            btnImportFromGallery.setOnClickListener {
                if (PermissionManager.isReadExternalPermissionGranted(this@ChatActivity)) {

                } else {
                    PermissionManager.requestReadExternalPermission(this@ChatActivity)
                }
            }

            btnEmoji.setOnClickListener {
            }

            btnRemoveCapturedImage.setOnClickListener {
                capturedImage = null
                layoutSelectedImage.visibility = View.GONE
            }
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
//                    val imageBitmap = data?.extras?.get("data") as Bitmap
//                    capturedImage = imageBitmap
                    if (capturedImage != null){
                        currentMessageType = MessageType.IMAGE

                        // show image
                        binding.layoutSelectExtras.visibility = View.GONE
                        binding.layoutSelectedImage.visibility = View.VISIBLE

                        Glide.with(this)
                            .load(capturedImage)
                            .into(binding.ivSelectedImage)
                    }
                }

            }
        }
    }
}