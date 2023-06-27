package com.mikirinkode.firebasechatapp.feature.chat.chatroom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper
import com.mikirinkode.firebasechatapp.commonhelper.ImageHelper
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentConversationBinding
import com.mikirinkode.firebasechatapp.feature.chat.fragment.ConversationFragmentArgs
import com.mikirinkode.firebasechatapp.feature.chat.fragment.ConversationFragmentDirections
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity
import com.mikirinkode.firebasechatapp.commonhelper.PermissionHelper

class ConversationFragment : Fragment(), ConversationView, ConversationAdapter.ChatClickListener {

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }

    private val conversationAdapter: ConversationAdapter by lazy {
        ConversationAdapter()
    }
    private val args: ConversationFragmentArgs by navArgs()

    private var isScrolledToBottom = false

    private var navigateFrom: String? = null // used to navigate back
    private var interlocutor: UserAccount? = null

    private lateinit var presenter: ConversationPresenter
    private var capturedImage: Uri? = null
    private var currentMessageType = MessageType.TEXT


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTextWatcher()
        initRecyclerView()
        handleBundleArgs()
        observeMessage()
        onActionClicked()
        onAppBatItemSelectedClickListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        presenter.detachView()
    }

    override fun onStop() {
        super.onStop()
        presenter.resetTotalUnreadMessage()
    }

    private fun initTextWatcher() {
        val handler = Handler(Looper.getMainLooper())
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // User is typing or text is changing
                args.conversationId?.let { presenter.updateTypingStatus(true, it) }
            }

            override fun afterTextChanged(s: Editable?) {
                // User has stopped typing
                handler.postDelayed({
                    args.conversationId?.let { presenter.updateTypingStatus(false, it) }
                }, 5000)

            }
        })
    }

    private fun initRecyclerView() {
        binding.apply {
            rvMessages.layoutManager = LinearLayoutManager(requireContext())
            rvMessages.adapter = conversationAdapter
            conversationAdapter.chatClickListener = this@ConversationFragment

            if (loggedUser?.userId != null) {
                conversationAdapter.setLoggedUserId(loggedUser?.userId!!)
                conversationAdapter.setConversationType(args.conversationType)
            }

            rvMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    binding.apply {
                        val layoutManager = rvMessages.layoutManager as LinearLayoutManager
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                        val itemCount = layoutManager.itemCount
                        // if the last item is not visible
                        if (lastVisibleItemPosition < itemCount - 1) { // TODO
                            btnScrollToBottom.visibility = View.VISIBLE
                            // show there is new message when user scrolling up
                            val totalUnread = conversationAdapter.getTotalUnreadMessageLoggedUser()
                            if (totalUnread > 0) {
                                tvTotalNewMessages.visibility = View.VISIBLE
                                tvTotalNewMessages.text = totalUnread.toString()
                            } else {
                                tvTotalNewMessages.visibility = View.GONE
                            }
                        } else {
                            tvTotalNewMessages.visibility = View.GONE
                            btnScrollToBottom.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }

    private fun handleBundleArgs() {
        val conversationType: String = args.conversationType
        navigateFrom = args.navigateFrom
        val conversationId: String? = args.conversationId

        when (conversationType) {
            ConversationType.PERSONAL.toString() -> {
                // if the message is empty, the conversationId is empty too
                // then use the args.interlocutorId
                // interlocutor id need for personal conversation to receive the user profile
                // even the conversation is not started yet
                if (conversationId.isNullOrBlank()) {
                    val loggedUserId = loggedUser?.userId
                    val interlocutorId = args.interlocutorId
                    if (loggedUserId != null && interlocutorId != null) {
                        val personalConversationId =
                            if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
                        setupPresenter(personalConversationId, conversationType)
                        presenter.getUserOnlineStatus(interlocutorId)
                        presenter.getConversationDataById(personalConversationId)
                    }
                } else {
                    setupPresenter(conversationId, conversationType)
                    presenter.getConversationDataById(conversationId)
                }
            }
            ConversationType.GROUP.toString() -> {
                if (conversationId != null) {
                    setupPresenter(conversationId, conversationType)
                    presenter.getConversationDataById(conversationId)
                }
            }
        }
    }

    private fun setupPresenter(conversationId: String, conversationType: String) {
        presenter = ConversationPresenter()
        presenter.attachView(this, requireActivity(), conversationId, conversationType)
    }

    private fun observeMessage() {
        presenter.receiveMessage()
    }

    private fun sendMessage() {
        binding.apply {
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

                val isValid: Boolean = senderId != null && senderName != null
                if (isValid) {
                    etMessage.setText("")
                    val isFirstTime: Boolean = conversationAdapter.isChatEmpty()
                    val receiverDeviceToken: List<String> =
                        conversationAdapter.getReceiverDeviceToken()

                    if (isFirstTime) {
                        val interlocutorId = interlocutor?.userId
                        if (interlocutorId != null) {
                            presenter.createPersonaChatRoom(senderId!!, interlocutorId)
                            Toast.makeText(
                                requireContext(),
                                "Chat Room Created",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    when (currentMessageType) {
                        MessageType.TEXT -> {
                            presenter.sendMessage(
                                message,
                                senderId!!,
                                senderName!!,
                                receiverDeviceToken
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
                                    senderId!!,
                                    senderName!!,
                                    capturedImage!!,
                                    path,
                                    receiverDeviceToken
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
    }


    private fun setupInterlocutorProfile(user: UserAccount) {
        binding.apply {
            interlocutor = user

            tvAppBarTitle.text = user.name
            if (user.avatarUrl != null && user.avatarUrl != "") {
                Glide.with(requireContext())
                    .load(user.avatarUrl)
                    .into(ivUserAvatar)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
            }
        }
    }

    private fun setupGroupProfile(conversation: Conversation) {
        binding.apply {
            tvAppBarTitle.text = conversation.conversationName
            if (conversation.conversationAvatar == null || conversation.conversationAvatar == "") {
                Glide.with(requireContext())
                    .load(R.drawable.ic_default_group_avatar).into(binding.ivUserAvatar)
            } else {
                Glide.with(requireContext())
                    .load(conversation.conversationAvatar)
                    .into(ivUserAvatar)
            }
        }
    }


    private fun setupPersonalTypingStatus(participants: List<UserAccount>) {
        val interlocutor = participants.firstOrNull { it.userId != loggedUser?.userId }
        val isInterlocutorTyping =
            conversationAdapter.getConversation()?.typingUser?.get(interlocutor?.userId)
        binding.apply {
            if (interlocutor != null) {

                if (isInterlocutorTyping == true) {
                    tvAppBarDescription.text = getString(R.string.txt_typing)
                } else if (interlocutor.online) {
                    tvAppBarDescription.text = getString(R.string.txt_online)
                    ivOnlineStatusIndicator.visibility = View.VISIBLE

                } else {
                    ivOnlineStatusIndicator.visibility = View.GONE
                    if (interlocutor.lastOnlineTimestamp != 0L) {
                        tvAppBarDescription.text =
                            "Last Online at ${DateHelper.getFormattedLastOnline(interlocutor.lastOnlineTimestamp)}"
                    }
                }
            }
        }
    }


    private fun setupGroupTypingStatus(participants: List<UserAccount>) {
        Log.e("ConversationFragment", "setupGroupTypingStatus")
        val typingUser = conversationAdapter.getConversation()?.typingUser

        val typingUserIdList: List<String>? = typingUser?.filterValues { it }?.keys?.toList()

        Log.e("ConversationFragment", "typing filtered: ${typingUserIdList}")
        Log.e("ConversationFragment", "participants: ${participants}")

        if (!typingUserIdList.isNullOrEmpty()) {
            val typingUsers = arrayListOf<UserAccount>()

            if (participants.isNotEmpty()) {
                for (user in participants) {
                    if (user.userId in typingUserIdList && user.userId != loggedUser?.userId) {
                        typingUsers.add(user)
                    }
                }
            }

            if (typingUsers.isNotEmpty()) {
                val nameAndStatus: String = typingUsers.joinToString("... ") { user ->
                    "${user.name} is typing"
                }
                binding.tvAppBarDescription.text = nameAndStatus
            }
        } else if (participants.isNotEmpty()) {
            val names: String = participants.joinToString(", ") { user ->
                user.name ?: ""
            }

            binding.tvAppBarDescription.text = names
        } else {
            binding.tvAppBarDescription.text = getString(R.string.txt_tap_to_see_group_info)
        }
    }

    override fun onConversationDataReceived(conversation: Conversation) {
        Log.e("ConversationFragment", "onConversationDataReceived")
        conversationAdapter.setConversation(conversation)
        when (args.conversationType) {
            ConversationType.GROUP.toString() -> {
                setupGroupProfile(conversation)
            }
            ConversationType.PERSONAL.toString() -> {

            }
        }

        binding.apply {
            val layoutManager = rvMessages.layoutManager as LinearLayoutManager
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val itemCount = layoutManager.itemCount
            // if the last item is not visible
            if (lastVisibleItemPosition < itemCount - 1) { // TODO
                btnScrollToBottom.visibility = View.VISIBLE
                // show there is new message when user scrolling up
                val totalUnread = conversationAdapter.getTotalUnreadMessageLoggedUser()
                if (totalUnread > 0) {
                    tvTotalNewMessages.visibility = View.VISIBLE
                    tvTotalNewMessages.text = totalUnread.toString()
                } else {
                    tvTotalNewMessages.visibility = View.GONE
                }
            } else {
                tvTotalNewMessages.visibility = View.GONE
                btnScrollToBottom.visibility = View.GONE
            }
        }
    }

    override fun onParticipantsDataReceived(participants: List<UserAccount>) {
        conversationAdapter.setParticipants(participants)
        when (args.conversationType) {
            ConversationType.PERSONAL.toString() -> {
                val user: UserAccount? =
                    participants.firstOrNull { it.userId != loggedUser?.userId }
                if (user != null) {
                    user.userId?.let { presenter.getUserOnlineStatus(it) }
                    setupInterlocutorProfile(user) // TODO: IT IS CALLED TWICE?
                }
                setupPersonalTypingStatus(participants)
            }
            ConversationType.GROUP.toString() -> {
                // setup group typing status
                setupGroupTypingStatus(participants)
            }
        }

    }

    override fun onMessagesReceived(messages: List<ChatMessage>) {
        conversationAdapter.setMessages(messages)
        if (messages.isNotEmpty()) {

            binding.apply {
                val layoutManager = binding.rvMessages.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val itemCount = layoutManager.itemCount
                // if the last visible item is not more than 5
                // ignore the isScrolledToBottom variable
                if (lastVisibleItemPosition in (itemCount - 5)..itemCount) { // TODO
                    if (conversationAdapter.itemCount != null && conversationAdapter.itemCount - 1 > 0) {
                        rvMessages.scrollToPosition(conversationAdapter.itemCount - 1)
                        isScrolledToBottom = true
                    }
                } else {
                    // the visible item is more than 2 item
                    if (conversationAdapter.itemCount != null && conversationAdapter.itemCount - 1 > 0 && !isScrolledToBottom) {
                        rvMessages.scrollToPosition(conversationAdapter.itemCount - 1)
                        isScrolledToBottom = true
                    }
                }
            }

        }
    }

    override fun updateReceiverOnlineStatus(status: UserAccount) {
        if (args.conversationType == ConversationType.PERSONAL.toString()) {
            if (loggedUser != null) {
                conversationAdapter.setParticipants(listOf(status, loggedUser!!))
            }

            setupInterlocutorProfile(status)
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

    override fun showOnUploadImageProgress(progress: Int) {
        binding.apply {
            progressBar.progress = progress
            if (progress == 100) {
                layoutUploadingProgress.visibility = View.GONE
                progressBar.visibility = View.GONE
            } else {
                layoutUploadingProgress.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    /**
     * Chat Click Listener
     */
    override fun onImageClick(chat: ChatMessage) {
        binding.apply {
            val action = ConversationFragmentDirections.actionOpenImage(
                chat.imageUrl,
                chat.message,
                chat.sendTimestamp,
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
            val totalSelectedMessages = conversationAdapter.getTotalSelectedMessages()
            if (totalSelectedMessages > 0) {
                appBarLayoutOnItemSelected.visibility = View.VISIBLE
                tvTotalSelectedMessages.text = totalSelectedMessages.toString()

                val selectedMessageSender =
                    conversationAdapter.getCurrentSelectedMessage()?.senderId

                if (totalSelectedMessages == 1 && selectedMessageSender == loggedUser?.userId) {
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
                conversationAdapter.onDeselectAllMessage()
            }

            btnShowMessageInfo.setOnClickListener {
                val totalSelectedMessages = conversationAdapter.getTotalSelectedMessages()
                val currentSelectedMessage = conversationAdapter.getCurrentSelectedMessage()
                val participantsId =
                    conversationAdapter.getConversation()?.participants?.keys?.toTypedArray()
                val conversationType = conversationAdapter.getConversation()?.conversationType
                val array: Array<String?> =
                    participantsId?.filterNotNull()?.toTypedArray() ?: arrayOfNulls<String>(0)

                val isValid =
                    participantsId != null && totalSelectedMessages == 1 && loggedUser?.userId != null && currentSelectedMessage != null && conversationType != null
                if (isValid) {
                    val action = ConversationFragmentDirections.actionShowMessageInfo(
                        loggedUser?.userId!!,
                        conversationType!!,
                        array,
                        currentSelectedMessage!!
                    )
                    Navigation.findNavController(binding.root).navigate(action)
                    conversationAdapter.onDeselectAllMessage()
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
                if (navigateFrom != null) {
                    requireActivity().finish()
                } else {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }
            }

            btnScrollToBottom.setOnClickListener {
                if (conversationAdapter.itemCount != null && conversationAdapter.itemCount - 1 > 0) {
                    rvMessages.smoothScrollToPosition(conversationAdapter.itemCount - 1)
                    presenter.resetTotalUnreadMessage()
                }
            }

            layoutInterlocutorProfile.setOnClickListener {
                when (args.conversationType) {
                    ConversationType.GROUP.toString() -> {
                        val conversationId = conversationAdapter.getConversation()?.conversationId
                        val participantsId =
                            conversationAdapter.getConversation()?.participants?.keys?.toTypedArray()
                        val creatorId = conversationAdapter.getConversation()?.createdBy
                        if (conversationId != null && participantsId != null && creatorId != null) {
                            val action =
                                ConversationFragmentDirections.actionOpenGroupProfile(
                                    conversationId,
                                    creatorId,
                                    participantsId
                                )
                            Navigation.findNavController(binding.root).navigate(action)
                        }
                    }
                    ConversationType.PERSONAL.toString() -> {
                        startActivity(
                            Intent(requireActivity(), ProfileActivity::class.java).putExtra(
                                ProfileActivity.EXTRA_INTENT_USER_ID,
                                interlocutor?.userId
                            )
                        )
                    }
                }
            }

            btnAddExtras.setOnClickListener {
                if (layoutSelectExtras.visibility == View.GONE) {
                    layoutSelectExtras.visibility = View.VISIBLE
                } else {
                    layoutSelectExtras.visibility = View.GONE
                }
            }

            btnSend.setOnClickListener {
                sendMessage()
            }

            btnCamera.setOnClickListener {
                if (PermissionHelper.isCameraPermissionGranted(requireContext())) {
                    presenter.takePicture()
                } else {
                    PermissionHelper.requestCameraPermission(requireActivity())
                }
            }

            btnImportFromGallery.setOnClickListener {
                if (PermissionHelper.isReadExternalPermissionGranted(requireContext())) {
                    // TODO: UNIMPLEMENTED

                } else {
                    PermissionHelper.requestReadExternalPermission(requireActivity())
                }
            }

            btnEmoji.setOnClickListener {
                // TODO: UNIMPLEMENTED
            }

            btnRemoveCapturedImage.setOnClickListener {
                capturedImage = null
                btnAddExtras.visibility = View.VISIBLE
                layoutSelectedImage.visibility = View.GONE
            }
        }
    }
}