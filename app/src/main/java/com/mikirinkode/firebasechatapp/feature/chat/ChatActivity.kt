package com.mikirinkode.firebasechatapp.feature.chat

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
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
    private var selectedFile: Uri? = null

    companion object {
        private const val CAMERA_REQUEST_CODE = 1
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
        val openedName = intent.getStringExtra(EXTRA_INTENT_OPENED_USER_NAME)

        setupReceiverProfile(openedName, openedAvatar)
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
            presenter.receiveMessage(loggedUserId = userId, openedUserId =  openedUserId!!)
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

    // TODO: try to move this function to presenter
    private fun checkGalleryPermission() {
        Dexter.withContext(this).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                gallery()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    this@ChatActivity,
                    "Anda belum memberikan perizinan untuk mengambil gambar",
                    Toast.LENGTH_SHORT
                ).show()
                showRotationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
                showRotationalDialogForPermission()
            }
        }).onSameThread().check()
    }

    private fun showRotationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("Sepertinya anda belum memberikan izin kamera atau galeri. Cek dan aktifkan perizinan dari pengaturan aplikasi.")

            .setPositiveButton("Setting") { _, _ ->

                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }

            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    // open gallery
    private fun gallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun showOnLongChatClickDialog() {
        Toast.makeText(this, "on long press", Toast.LENGTH_SHORT).show()

    }

    override fun updateMessages(messages: List<ChatMessage>) {
        chatAdapter.setData(messages)
        if (messages.isNotEmpty()){
            binding.rvMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun updateReceiverOnlineStatus(status: UserOnlineStatus) {

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
                    if (senderId != null && openedUserId != null) {
                        etMessage.setText("")
                        if (selectedFile != null) {
                            val imageExtension = MimeTypeMap.getSingleton()
                                .getExtensionFromMimeType(
                                    contentResolver.getType(
                                        selectedFile!!
                                    )
                                )

                            val path =
                                "messages/images-" + System.currentTimeMillis() + "." + imageExtension
                            presenter.sendMessage(
                                message,
                                senderId,
                                openedUserId!!,
                                selectedFile!!,
                                path
                            )
                            binding.layoutSelectExtras.visibility = View.GONE
                            binding.layoutSelectedImage.visibility = View.GONE
                            selectedFile = null
                        } else {
                            presenter.sendMessage(message, senderId, openedUserId!!)
                        }
                    }
                }
            }

            btnCamera.setOnClickListener {
                // TODO: Handle later
                Toast.makeText(this@ChatActivity, "Unimplemented", Toast.LENGTH_SHORT)
                    .show() // TODO: Remove later
            }

            btnCamera.setOnLongClickListener {
                showOnLongChatClickDialog()
                true
            }

            btnImportFromGallery.setOnClickListener {
                checkGalleryPermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap

                    Glide.with(this)
                        .load(bitmap)
                        .into(binding.ivSelectedImage)

                    // TODO: SAVE BITMAP TO DEVICE AND GET THE URI
                }

                GALLERY_REQUEST_CODE -> {
                    if (data?.data != null) {
                        val file: Uri? = data.data
                        selectedFile = data.data

                        binding.layoutSelectExtras.visibility = View.GONE
                        binding.layoutSelectedImage.visibility = View.VISIBLE

                        Glide.with(this)
                            .load(data.data)
                            .into(binding.ivSelectedImage)
                    }
                }
            }
        }
    }
}