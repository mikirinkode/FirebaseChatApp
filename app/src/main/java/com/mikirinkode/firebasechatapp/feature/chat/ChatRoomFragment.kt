package com.mikirinkode.firebasechatapp.feature.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.databinding.FragmentChatRoomBinding
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity
import com.mikirinkode.firebasechatapp.helper.DateHelper
import com.mikirinkode.firebasechatapp.helper.ImageHelper
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class ChatRoomFragment : Fragment(), ChatView, ChatAdapter.ChatClickListener {

    private var _binding: FragmentChatRoomBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val chatAdapter: ChatAdapter by lazy {
        ChatAdapter()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(DataConstant.USER, UserAccount::class.java)
    }

    private var interlocutor: UserAccount? = null

    private lateinit var presenter: ChatPresenter
    private var capturedImage: Uri? = null
    private var currentMessageType = MessageType.TEXT

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
        onAppBatItemSelectedClickListener()
    }

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

            if (loggedUser?.userId != null) {
                chatAdapter.setLoggedUserId(loggedUser?.userId!!)
            }
        }
    }

    private fun handleBundleArgs() {
        val args: ChatRoomFragmentArgs by navArgs()
        val interlocutorId = args.interlocutorId
        val loggedUserId = loggedUser?.userId
        setupPresenter(
            loggedUserId,
            interlocutorId
        ) // call setup presenter after get the interlocutor user id
    }

    private fun setupPresenter(loggedUserId: String?, interlocutorId: String?) {
        presenter = ChatPresenter()
        presenter.attachView(this)

        if (loggedUserId != null && interlocutorId != null) {
            presenter.onInit(requireActivity(), loggedUserId, interlocutorId)
            presenter.getUserOnlineStatus(interlocutorId)
            presenter.getInterlocutorData(interlocutorId)
        }
    }

    private fun observeMessage() {
        presenter.receiveMessage()
    }

    override fun onMessagesReceived(messages: List<ChatMessage>) {
        chatAdapter.setData(messages)
        if (messages.isNotEmpty()) {
            if (chatAdapter.itemCount - 1 > 0){
                // TODO: check again later, error: NullPointerException
                binding.rvMessages.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }

    override fun onGetInterlocutorProfileSuccess(user: UserAccount) {
        binding.apply {
            interlocutor = user

            tvName.text = user.name
            if (user.avatarUrl != null && user.avatarUrl != "") {
                Glide.with(requireContext())
                    .load(user.avatarUrl)
                    .into(ivUserAvatar)
            }
        }
    }

    override fun updateReceiverOnlineStatus(status: UserRTDB) {
        binding.apply {
            if (status.online) {
                tvUserStatus.text = "Online"
                ivOnlineStatusIndicator.visibility = View.VISIBLE
            } else {
                ivOnlineStatusIndicator.visibility = View.GONE
                tvUserStatus.text =
                    "Last Online at ${DateHelper.getFormattedLastOnline(status.lastOnlineTimestamp)}"
            }
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
    override fun onImageClick(chat: ChatMessage) {
        binding.apply {
            val action = ChatRoomFragmentDirections.actionOpenImage(
                chat.imageUrl,
                chat.message,
                chat.timestamp,
                chat.senderName
            )
            Navigation.findNavController(binding.root).navigate(action)
        }
    }


    override fun onMessageSelected() {
        updateAppBarOnSelectedView()
    }

    override fun onMessageDeselect() {
        updateAppBarOnSelectedView()
    }

    private fun updateAppBarOnSelectedView() {
        binding.apply {
            val totalSelectedMessages = chatAdapter.getTotalSelectedMessages()
            if (totalSelectedMessages > 0) {
                appBarLayoutOnItemSelected.visibility = View.VISIBLE
                tvTotalSelectedMessages.text = totalSelectedMessages.toString()

                if (totalSelectedMessages == 1) {
                    btnShowMessageInfo.visibility = View.VISIBLE
                } else {
                    btnShowMessageInfo.visibility = View.GONE
                }
            } else {
                appBarLayoutOnItemSelected.visibility = View.GONE
            }
        }
    }


    private fun onAppBatItemSelectedClickListener() {
        binding.apply {

            btnBackOnItemSelected.setOnClickListener {
                appBarLayoutOnItemSelected.visibility = View.GONE
                chatAdapter.onDeselectAllMessage()
            }

            btnShowMessageInfo.setOnClickListener { // TODO
                val totalSelectedMessages = chatAdapter.getTotalSelectedMessages()
                val currentSelectedMessage = chatAdapter.getCurrentSelectedMessage()

                if (totalSelectedMessages == 1 && loggedUser?.userId != null && currentSelectedMessage != null) {
                    val action = ChatRoomFragmentDirections.actionShowMessageInfo(
                        loggedUser?.userId!!,
                        currentSelectedMessage
                    )
                    Navigation.findNavController(binding.root).navigate(action)
                    chatAdapter.onDeselectAllMessage()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "There is no selected message.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun onActionClicked() {
        binding.apply {
            btnBack.setOnClickListener {
                requireActivity().finish()
            }

            layoutInterlocutorProfile.setOnClickListener {
                startActivity(
                    Intent(requireActivity(), ProfileActivity::class.java).putExtra(
                        ProfileActivity.EXTRA_INTENT_USER_ID,
                        interlocutor?.userId
                    )
                )
            }

            btnAddExtras.setOnClickListener { // TODO
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
                    val senderId = loggedUser?.userId
                    val senderName = loggedUser?.name

                    val interlocutorId = interlocutor?.userId
                    val interlocutorName = interlocutor?.name

                    val isValid: Boolean =
                        senderId != null && senderName != null && interlocutorId != null && interlocutorName != null

                    val isFirstTime: Boolean = chatAdapter.isChatEmpty()
                    if (isFirstTime){
                        Toast.makeText(requireContext(), "first time chat congrats", Toast.LENGTH_SHORT).show()
                    }

                    if (senderId != null && senderName != null && interlocutorId != null && interlocutorName != null) {
                        etMessage.setText("")
                        when (currentMessageType) {
                            MessageType.TEXT -> {
                                presenter.sendMessage(
                                    message,
                                    senderId,
                                    interlocutorId,
                                    senderName,
                                    interlocutorName,
                                    isFirstTime
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
                                        interlocutorId,
                                        senderName,
                                        interlocutorName,
                                        capturedImage!!,
                                        path,
                                        isFirstTime
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