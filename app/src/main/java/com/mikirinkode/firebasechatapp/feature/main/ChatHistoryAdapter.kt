package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
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
                val latestTimestamp: Long? = conversation.messages.maxByOrNull { it.value.timestamp }?.value?.timestamp
                val latestMessage: ChatMessage? =
                    conversation.messages.values.first { it.timestamp == latestTimestamp }

                // TODO: create date helper
                val timestamp = Timestamp(latestMessage?.timestamp ?: 0)
                val date = Date(timestamp.time)
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val time = dateFormat.format(date)


                tvTimestamp.text = time
                tvMessage.text = latestMessage?.message
                tvUserName.text = conversation.interlocutor?.name

                if (conversation.interlocutor?.avatarUrl != null && conversation.interlocutor?.avatarUrl != ""){
                    Glide.with(itemView.context)
                        .load(conversation.interlocutor?.avatarUrl).into(binding.ivUserAvatar)
                }

                if (latestMessage?.senderId == loggedUserId){
                    if (latestMessage.beenRead){
                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "✓✓"
                        tvMessageStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.message_been_read_color, null))
                    }
                    if (!latestMessage.beenRead && latestMessage.deliveredTimestamp != 0L){
                        val currentNightMode = itemView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

                        when (currentNightMode) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                // The system is currently in night mode
                                tvMessageStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.night_theme_text_color, null))
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                // The system is currently in day mode
                                tvMessageStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.light_theme_text_color, null))
                            }
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                // We don't know what mode we're in, assume day mode
                                tvMessageStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.light_theme_text_color, null))
                            }
                        }

                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "✓✓"

                    }

                    if (!latestMessage.beenRead && latestMessage.deliveredTimestamp == 0L && latestMessage.timestamp != 0L){
                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "✓"
                    }
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