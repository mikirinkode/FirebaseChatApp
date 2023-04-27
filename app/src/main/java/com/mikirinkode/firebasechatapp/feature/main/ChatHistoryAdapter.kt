package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.databinding.ItemChatHistoryBinding
import com.mikirinkode.firebasechatapp.feature.chat.ChatActivity
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatHistoryAdapter : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

    private val conversations: ArrayList<Conversation> = ArrayList()
    private var loggedUserId: String = ""

    inner class ViewHolder(private val binding: ItemChatHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.apply {
                // TODO: create date helper
                val timestamp = Timestamp(conversation.lastMessageTimestamp)
                val date = Date(timestamp.time)
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val time = dateFormat.format(date)

                tvTimestamp.text = time
                tvMessage.text = conversation.lastMessage
                tvUserName.text = conversation.interlocutor?.name
                if (conversation.interlocutor?.avatarUrl != null && conversation.interlocutor?.avatarUrl != ""){
                    Glide.with(itemView.context)
                        .load(conversation.interlocutor?.avatarUrl).into(binding.ivUserAvatar)
                }


            }
            itemView.setOnClickListener {
                itemView.context.startActivity(
                    Intent(
                        itemView.context,
                        ChatActivity::class.java
                    )
                        .putExtra(ChatActivity.EXTRA_INTENT_OPENED_USER_ID, conversation.interlocutor?.userId)
                        .putExtra(ChatActivity.EXTRA_INTENT_OPENED_USER_AVATAR, conversation.interlocutor?.avatarUrl)
                        .putExtra(ChatActivity.EXTRA_INTENT_OPENED_USER_NAME, conversation.interlocutor?.name)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    fun setLoggedUserId(userId: String){
        loggedUserId = userId
    }

    fun setData(newList: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newList)
        notifyDataSetChanged()
    }
}