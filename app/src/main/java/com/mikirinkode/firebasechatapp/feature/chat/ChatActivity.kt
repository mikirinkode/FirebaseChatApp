package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.data.model.UserOnlineStatus
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity(), ChatView {

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
    private var receiverId: String? = null

    companion object {

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
        if (receiverId != null){
            presenter.getUserOnlineStatus(receiverId!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun handleIntent() {
        receiverId = intent.getStringExtra("key_receiver_id")
        val receiverAvatar = intent.getStringExtra("key_receiver_avatar")
        val receiverName = intent.getStringExtra("key_receiver_name")

        setupReceiverProfile(receiverName, receiverAvatar)
    }


    private fun initView() {
        binding.apply {

            val userId = pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
            rvMessages.layoutManager = LinearLayoutManager(this@ChatActivity)
            rvMessages.adapter = chatAdapter
            if (userId != null) {
                chatAdapter.setLoggedUserId(userId)
            }
        }
    }

    private fun observeMessage(){
        val userId = pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
        if (userId != null && receiverId != null) {
            presenter.receiveMessage(receiverId!!, userId)
        }
    }

    private fun setupReceiverProfile(receiverName: String?, receiverAvatar: String?) {
        binding.apply {
            tvName.text = receiverName
            if (receiverAvatar != null && receiverAvatar != ""){
                Glide.with(this@ChatActivity)
                    .load(receiverAvatar)
                    .into(ivUserAvatar)
            }
        }
    }
    private fun setupPresenter(){
        presenter = ChatPresenter()
        presenter.attachView(this)
        if (receiverId != null){
            presenter.getUserOnlineStatus(receiverId!!)
        }
    }

    override fun updateMessages(messages: List<ChatMessage>) {
        chatAdapter.setData(messages)
    }

    override fun updateReceiverOnlineStatus(status: UserOnlineStatus) {
        Log.e("ChatActivity", "receiver status: $status")
        Log.e("ChatActivity", "receiver status: ${status.online}")
        Log.e("ChatActivity", "receiver status: ${status.lastOnlineTimestamp}")
        if (status.online){
            binding.tvUserStatus.text = "Online"
        } else {
            val timestamp = Timestamp(status.lastOnlineTimestamp)
            val date = Date(timestamp.time)
            val dateFormat = SimpleDateFormat("dd MMMM yyyy hh:mm a", Locale.getDefault())
            val formattedDate = dateFormat.format(date)

            binding.tvUserStatus.text = "Last Online at $formattedDate"
        }
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    private fun onActionClicked(){
        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }

            btnSend.setOnClickListener {
                val message = etMessage.text.toString().trim()
                if (message.isNotBlank()){
                    val senderId = pref?.getObject(DataConstant.USER, UserAccount::class.java)?.userId
                    if (senderId != null && receiverId != null){
                        etMessage.setText("")
                        presenter.sendMessage(message, senderId, receiverId!!)
                    }
                }
            }
        }
    }
}