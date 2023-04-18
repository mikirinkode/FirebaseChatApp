package com.mikirinkode.firebasechatapp.feature.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.databinding.ActivityChatBinding
import com.mikirinkode.firebasechatapp.databinding.ActivityMainBinding

class ChatActivity : AppCompatActivity(), ChatView {

    private val binding: ActivityChatBinding by lazy {
        ActivityChatBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: ChatPresenter

    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        handleIntent()
        setupPresenter()
        initView()
        onActionClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun handleIntent() {
        val receiverAvatar = intent.getStringExtra("key_receiver_avatar")
        val receiverName = intent.getStringExtra("key_receiver_name")

        setupReceiverProfile(receiverName, receiverAvatar)
    }


    private fun initView() {}

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

    override fun showLoading() {
        TODO("Not yet implemented")
    }

    override fun hideLoading() {
        TODO("Not yet implemented")
    }

    private fun onActionClicked(){
        binding.apply {
            btnBack.setOnClickListener { onBackPressed() }

            btnSend.setOnClickListener {

            }
        }
    }
}