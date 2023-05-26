package com.mikirinkode.firebasechatapp.feature.chat

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.databinding.FragmentChatRoomBinding
import com.mikirinkode.firebasechatapp.helper.DateHelper
import com.mikirinkode.firebasechatapp.helper.ImageHelper
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class ChatRoomFragment : Fragment(), ChatView, ChatAdapter.ChatClickListener {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val loggedUserId: String? by lazy {
        pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
    }

    private val chatAdapter: ChatAdapter by lazy {
        ChatAdapter()
    }

    private lateinit var presenter: ChatPresenter
    private var openedUserId: String? = null
    private var openedUserName: String? = null

    private var capturedImage: Uri? = null
    private var currentMessageType = MessageType.TEXT
    private var totalSelectedMessages: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        handleBundleArgs()
        observeMessage()
        onActionClicked()
    }

//    override fun onResume() {
//        super.onResume()
//        if (openedUserId != null) {
//            presenter.getUserOnlineStatus(openedUserId!!)
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        presenter.detachView()
    }

    private fun initRecyclerView() {
        binding.apply {
            rvMessages.layoutManager = LinearLayoutManager(requireContext())
            rvMessages.adapter = chatAdapter
            chatAdapter.chatClickListener = this@ChatRoomFragment

            if (loggedUserId != null) {
                chatAdapter.setLoggedUserId(loggedUserId!!)
            }
        }
    }

    private fun handleBundleArgs() {
        val args: ChatRoomFragmentArgs by navArgs()
        openedUserId = args.openedUserId
        openedUserName = args.openedUserName
        val openedAvatar = args.openedUserAvatar

        setupPresenter() // call setup presenter after get the opened user id
        setupInterlocutorProfile(openedUserName, openedAvatar)
    }

    private fun setupInterlocutorProfile(openedName: String?, openedAvatar: String?) {
        binding.apply {
            tvName.text = openedName
            if (openedAvatar != null && openedAvatar != "") {
                Glide.with(requireContext())
                    .load(openedAvatar)
                    .into(ivUserAvatar)
            }
        }
    }

    private fun setupPresenter() {
        presenter = ChatPresenter()
        presenter.attachView(this)
        if (loggedUserId != null && openedUserId != null) {
            presenter.onInit(requireActivity(), loggedUserId!!, openedUserId!!)
            presenter.getUserOnlineStatus(openedUserId!!)
        }
    }

    private fun observeMessage() {
        presenter.receiveMessage()
    }

    override fun updateMessages(messages: List<ChatMessage>) {
        chatAdapter.setData(messages)
        if (messages.isNotEmpty()) {
            binding.rvMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun updateReceiverOnlineStatus(status: UserRTDB) {
        if (status.online) {
            binding.tvUserStatus.text = "Online"
            binding.ivOnlineStatusIndicator.visibility = View.VISIBLE
        } else {
            binding.ivOnlineStatusIndicator.visibility = View.GONE
            binding.tvUserStatus.text =
                "Last Online at ${DateHelper.getFormattedLastOnline(status.lastOnlineTimestamp)}"
        }
    }

    override fun onImageCaptured(capturedImage: Uri?) {
        this.capturedImage = capturedImage

        currentMessageType = MessageType.IMAGE
        binding.apply {
            btnAddExtras.visibility = View.GONE
            layoutSelectExtras.visibility = View.GONE
            layoutSelectedImage.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(capturedImage)
                .into(ivSelectedImage)
        }
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    /**
     * Chat Click Listener
     */
    override fun onImageClick(chat: ChatMessage) { // OPEN FRAGMENT
        binding.apply {
            val action = ChatRoomFragmentDirections.actionOpenImage(chat.imageUrl, chat.message, chat.timestamp, chat.senderName)
            Navigation.findNavController(binding.root).navigate(action)
//            layoutDetailImage.root.visibility = View.VISIBLE
//            layoutDetailImage.tvMessageOnDetailImage.text = chat.message
//
//            layoutDetailImage.tvUserName.text = chat.senderName
//            layoutDetailImage.tvDate.text =
//                DateHelper.getRegularFormattedDateTimeFromTimestamp(chat.timestamp)
//
//            Glide.with(requireContext())
//                .load(chat.imageUrl)
//                .into(layoutDetailImage.ivDetailImage)
//
//            layoutDetailImage.btnBack.setOnClickListener {
//                layoutDetailImage.root.visibility = View.GONE
//            }
        }
    }

    override fun onLongClick(chat: ChatMessage) {
//        showOnLongChatClickDialog()
        Log.e("ChatActivity", "on long click: $totalSelectedMessages")

    }

    override fun onMessageSelected() {
        totalSelectedMessages += 1
        Log.e("ChatActivity", "on message selected: $totalSelectedMessages")
        binding.apply {
            if (totalSelectedMessages > 0) {
                appBarLayoutOnItemSelected.visibility = View.VISIBLE
                tvTotalSelectedMessages.text = totalSelectedMessages.toString()
            } else {
                appBarLayoutOnItemSelected.visibility = View.GONE
            }
        }
    }
    override fun onMessageDeselect() {
        totalSelectedMessages -= 1
        Log.e("ChatActivity", "on message deselected: $totalSelectedMessages")
        binding.apply {
            if (totalSelectedMessages > 0) {
                appBarLayoutOnItemSelected.visibility = View.VISIBLE
                tvTotalSelectedMessages.text = totalSelectedMessages.toString()
            } else {
                appBarLayoutOnItemSelected.visibility = View.GONE
            }
        }
    }



    private fun onActionClicked() {
        binding.apply {
            btnBack.setOnClickListener {
                requireActivity().finish()
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
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

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
                                    val path = ImageHelper.getPathForMessages(
                                        requireActivity().contentResolver,
                                        capturedImage!!
                                    )
                                    presenter.sendMessage(
                                        message,
                                        senderId,
                                        openedUserId!!,
                                        senderName!!,
                                        openedUserName!!,
                                        capturedImage!!,
                                        path
                                    )
                                    btnAddExtras.visibility = View.VISIBLE
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
                if (PermissionManager.isCameraPermissionGranted(requireContext())) {
                    // Open Camera
                    presenter.takePicture()
                } else {
                    PermissionManager.requestCameraPermission(requireActivity())
                }
            }

            btnImportFromGallery.setOnClickListener {
                if (PermissionManager.isReadExternalPermissionGranted(requireContext())) {

                } else {
                    PermissionManager.requestReadExternalPermission(requireActivity())
                }
            }

            btnEmoji.setOnClickListener {
                // TODO
            }

            btnRemoveCapturedImage.setOnClickListener {
                capturedImage = null
                btnAddExtras.visibility = View.VISIBLE
                layoutSelectedImage.visibility = View.GONE
            }
        }
    }
}