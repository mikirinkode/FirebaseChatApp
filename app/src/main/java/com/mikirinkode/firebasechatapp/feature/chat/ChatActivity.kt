package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding

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
    }

    override fun updateMessages(messages: List<ChatMessage>) {
        chatAdapter.setData(messages)
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