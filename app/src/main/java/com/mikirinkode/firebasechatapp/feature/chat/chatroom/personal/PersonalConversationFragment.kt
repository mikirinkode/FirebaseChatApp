package com.mikirinkode.firebasechatapp.feature.chat.chatroom.personal

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
import com.mikirinkode.firebasechatapp.commonhelper.ImageHelper
import com.mikirinkode.firebasechatapp.commonhelper.PermissionHelper
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.local.pref.PreferenceConstant
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.OnlineStatus
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.FragmentPersonalConversationBinding
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.feature.profile.ProfileActivity

/**
 * TODO: UPDATE PUSH NOTIFICATION
 * ADD CONVERSATION TYPE
 */
class PersonalConversationFragment : Fragment(), PersonalConversationView,
    PersonalConversationAdapter.ChatClickListener {

    private var _binding: FragmentPersonalConversationBinding? = null
    private val binding get() = _binding!!

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private val loggedUser: UserAccount? by lazy {
        pref?.getObject(PreferenceConstant.USER, UserAccount::class.java)
    }
    private var interlocutor: UserAccount? = null

    private val args: PersonalConversationFragmentArgs by navArgs()

    private val adapter: PersonalConversationAdapter by lazy {
        PersonalConversationAdapter()
    }

    private var presenter: PersonalConversationPresenter? = null

    private var capturedImage: Uri? = null
    private var currentMessageType = MessageType.TEXT
    private var isScrolledToBottom = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPersonalConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setupPresenter()
        initTextWatcher()
        onActionClicked()
        onAppBatItemSelectedClickListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                Log.e("PersonalConversationFragment", "onTextChanged")
                // User is typing or text is changing
                presenter?.updateTypingStatus(true, args.interlocutorId, args.conversationId)
            }

            override fun afterTextChanged(s: Editable?) {
                // User has stopped typing
                handler.postDelayed({
                    presenter?.updateTypingStatus(false, args.interlocutorId, args.conversationId)
                }, 5000)

            }
        })
    }

    private fun initRecyclerView() {
        binding.apply {
            rvMessages.layoutManager = LinearLayoutManager(requireContext())
            rvMessages.adapter = adapter
            adapter.chatClickListener = this@PersonalConversationFragment

            if (loggedUser?.userId != null) {
                adapter.setLoggedUserId(loggedUser?.userId!!)
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
                            val totalUnread = adapter.getTotalUnreadMessageLoggedUser()
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


    private fun setupPresenter() {
        presenter = PersonalConversationPresenter(
            this,
            args.conversationId,
            args.interlocutorId,
            requireActivity()
        )
        presenter?.getInterlocutorData(args.interlocutorId)
        presenter?.receiveMessage()
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
                    val isFirstTime: Boolean = adapter.isChatEmpty()
                    val receiverDeviceToken: List<String> =
                        adapter.getReceiverDeviceToken()

                    if (isFirstTime) {
                        val interlocutorId = interlocutor?.userId
                        if (interlocutorId != null) {
                            presenter?.createPersonaChatRoom(senderId!!, interlocutorId)
                            Toast.makeText(
                                requireContext(),
                                "Chat Room Created",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    when (currentMessageType) {
                        MessageType.TEXT -> {
                            presenter?.sendMessage(
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
                                presenter?.sendMessage(
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

    override fun onInterlocutorDataReceived(user: UserAccount) {
        binding.apply {
            interlocutor = user
            tvInterlocutorName.text = user.name
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

    // TODO: DUPLICATE LIKE IN GROUP
    override fun onMessagesReceived(messages: List<ChatMessage>) {
        adapter.setMessages(messages)
        if (messages.isNotEmpty()) {

            binding.apply {
                val layoutManager = binding.rvMessages.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val itemCount = layoutManager.itemCount
                // if the last visible item is not more than 5
                // ignore the isScrolledToBottom variable
                if (lastVisibleItemPosition in (itemCount - 5)..itemCount) { // TODO
                    if (adapter.itemCount != null && adapter.itemCount - 1 > 0) {
                        rvMessages.scrollToPosition(adapter.itemCount - 1)
                        isScrolledToBottom = true
                    }
                } else {
                    // the visible item is more than 2 item
                    if (adapter.itemCount != null && adapter.itemCount - 1 > 0 && !isScrolledToBottom) {
                        rvMessages.scrollToPosition(adapter.itemCount - 1)
                        isScrolledToBottom = true
                    }
                }
            }

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

    /**
     * Chat Click Listener
     */
    // TODO: DUPLICATE LIKE IN GROUP
    override fun onImageClick(chat: ChatMessage) {
        binding.apply {
            val action = PersonalConversationFragmentDirections.actionOpenImage(
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
            val totalSelectedMessages = adapter.getTotalSelectedMessages()
            if (totalSelectedMessages > 0) {
                appBarLayoutOnItemSelected.visibility = View.VISIBLE
                tvTotalSelectedMessages.text = totalSelectedMessages.toString()

                val selectedMessageSender =
                    adapter.getCurrentSelectedMessage()?.senderId

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
                adapter.onDeselectAllMessage()
            }

            btnShowMessageInfo.setOnClickListener {
                if (loggedUser != null && loggedUser?.userId != null) {
                    val totalSelectedMessages = adapter.getTotalSelectedMessages()
                    val currentSelectedMessage = adapter.getCurrentSelectedMessage()

                    val participantsId =
                        listOf(loggedUser!!.userId!!, args.interlocutorId).toTypedArray()

                    val conversationType = ConversationType.PERSONAL.toString()

                    val isValid =
                        totalSelectedMessages == 1 && loggedUser?.userId != null && currentSelectedMessage != null
                    if (isValid) {
                        val action = PersonalConversationFragmentDirections.actionShowMessageInfo(
                            loggedUser?.userId!!,
                            conversationType,
                            participantsId,
                            currentSelectedMessage!!
                        )
                        Navigation.findNavController(binding.root).navigate(action)
                        adapter.onDeselectAllMessage()
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
    }

    private fun onActionClicked() {
        binding.apply {
            btnBack.setOnClickListener {
                if (args.navigateFrom != null) {
                    requireActivity().finish()
                } else {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finishAffinity()
                }
            }

            btnScrollToBottom.setOnClickListener {
                if (adapter.itemCount != null && adapter.itemCount - 1 > 0) {
                    rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
                    presenter?.resetTotalUnreadMessage()
                }
            }

            layoutInterlocutorProfile.setOnClickListener {
                startActivity(
                    Intent(requireActivity(), ProfileActivity::class.java).putExtra(
                        ProfileActivity.EXTRA_INTENT_USER_ID,
                        interlocutor?.userId
                    )
                )
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
                    presenter?.takePicture()
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

