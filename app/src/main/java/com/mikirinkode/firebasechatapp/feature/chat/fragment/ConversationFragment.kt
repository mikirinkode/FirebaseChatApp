package com.mikirinkode.firebasechatapp.feature.chat.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.mikirinkode.firebasechatapp.data.model.UserRTDB
import com.mikirinkode.firebasechatapp.databinding.FragmentConversationBinding
import com.mikirinkode.firebasechatapp.feature.chat.*
import com.mikirinkode.firebasechatapp.feature.chat.adapter.ConversationAdapter
import com.mikirinkode.firebasechatapp.feature.chat.presenter.ConversationPresenter
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity
import com.mikirinkode.firebasechatapp.firebase.CommonFirebaseTaskHelper
import com.mikirinkode.firebasechatapp.utils.PermissionManager

class ConversationFragment : Fragment(), ConversationView, ConversationAdapter.ChatClickListener {

    private var _binding: FragmentConversationBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val conversationAdapter: ConversationAdapter by lazy {
        ConversationAdapter()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }


    private val mCommonHelper: CommonFirebaseTaskHelper by lazy {
        CommonFirebaseTaskHelper()
    }

    private val args: ConversationFragmentArgs by navArgs()

    private var navigateFrom: String? = null // used to navigate back
    private var interlocutor: UserAccount? = null
    private var conversation: Conversation? = null

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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        presenter.detachView()
    }

    private fun initTextWatcher() {
        val handler = Handler(Looper.getMainLooper())
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // User is typing or text is changing
                interlocutor?.userId?.let { mCommonHelper.updateTypingStatus(true, it) }

            }

            override fun afterTextChanged(s: Editable?) {
                // User has stopped typing
                handler.postDelayed({
                    interlocutor?.userId?.let { mCommonHelper.updateTypingStatus(false, it) }
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
        }
    }

    private fun handleBundleArgs() {
        val conversationType: String = args.conversationType
        val participantIdList: List<String> = listOf()
        navigateFrom = args.navigateFrom

        when (conversationType) {
            ConversationType.PERSONAL.toString() -> {
                val interlocutorId = args.interlocutorId
                val loggedUserId = loggedUser?.userId
                if (loggedUserId != null) {
                    val conversationId =
                        if (interlocutorId < loggedUserId) "$interlocutorId-$loggedUserId" else "$loggedUserId-$interlocutorId"
                    setupPresenter(conversationId, conversationType)
                    getInterlocutorData(interlocutorId)
                }
            }
            ConversationType.GROUP.toString() -> {
                val conversationId: String? = args.conversationId
                if (conversationId != null) {
                    setupPresenter(conversationId, conversationType)
                    getGroupData(conversationId)
                }
            }
        }
    }

    private fun setupPresenter(conversationId: String, conversationType: String) {
        presenter = ConversationPresenter()
        presenter.attachView(this, requireActivity(), conversationId, conversationType)
    }

    private fun getInterlocutorData(interlocutorId: String) {
        presenter.getUserOnlineStatus(interlocutorId)
        presenter.getInterlocutorData(interlocutorId)
    }

    private fun getGroupData(conversationId: String) {
        presenter.getGroupData(conversationId)
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
//                when (args.conversationType) {
//                    ConversationType.PERSONAL.toString() -> handlePersonalMessage(message)
//                    ConversationType.GROUP.toString() -> handleGroupMessage(message)
//                }
            }
        }
    }

    override fun onReceiveGroupData(conversation: Conversation) {
        this.conversation = conversation
        binding.apply {
            conversationAdapter.setParticipantIdList(conversation.participants)
            tvAppBarTitle.text = conversation.conversationName
            tvAppBarDescription.text = getString(R.string.txt_tap_to_see_group_info)
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

    override fun onGetInterlocutorProfileSuccess(user: UserAccount) {
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

    override fun onMessagesReceived(messages: List<ChatMessage>) {
        conversationAdapter.setData(messages)
        if (messages.isNotEmpty()) {
            if (conversationAdapter.itemCount != null && conversationAdapter.itemCount - 1 > 0) {
                // TODO: check again later, error: NullPointerException
                binding.rvMessages.smoothScrollToPosition(conversationAdapter.itemCount - 1)
            }
        }
    }

    override fun updateReceiverOnlineStatus(status: UserRTDB) {
        binding.apply {
            if (status.online) {
                tvAppBarDescription.text = getString(R.string.txt_online)
                ivOnlineStatusIndicator.visibility = View.VISIBLE

                if (status.typing && status.currentlyTypingFor == loggedUser?.userId) {
                    tvAppBarDescription.text = getString(R.string.txt_typing)
                }
            } else {
                ivOnlineStatusIndicator.visibility = View.GONE
                tvAppBarDescription.text =
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

            btnShowMessageInfo.setOnClickListener { // TODO
                val totalSelectedMessages = conversationAdapter.getTotalSelectedMessages()
                val currentSelectedMessage = conversationAdapter.getCurrentSelectedMessage()

                if (totalSelectedMessages == 1 && loggedUser?.userId != null && currentSelectedMessage != null) {
                    val action = ConversationFragmentDirections.actionShowMessageInfo(
                        loggedUser?.userId!!,
                        currentSelectedMessage
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

            layoutInterlocutorProfile.setOnClickListener {
                when (args.conversationType) {
                    ConversationType.GROUP.toString() -> {
                        if (conversation != null){
                            val action =
                                ConversationFragmentDirections.actionOpenGroupProfile(conversation!!)
                            Navigation.findNavController(binding.root).navigate(action)
                        }
                    }
                    ConversationType.PERSONAL.toString() -> { // TODO
                        startActivity(
                            Intent(requireActivity(), ProfileActivity::class.java).putExtra(
                                ProfileActivity.EXTRA_INTENT_USER_ID,
                                interlocutor?.userId
                            )
                        )
                    }
                }
            }

            btnAddExtras.setOnClickListener { // TODO
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