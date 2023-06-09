package com.mikirinkode.firebasechatapp.feature.main

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.databinding.ItemChatHistoryBinding
import com.mikirinkode.firebasechatapp.feature.chat.GroupChatActivity
import com.mikirinkode.firebasechatapp.feature.chat.PersonalChatActivity
import com.mikirinkode.firebasechatapp.helper.DateHelper
import kotlin.collections.ArrayList

class ChatHistoryAdapter : RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder>() {

    private val conversations: ArrayList<Conversation> = ArrayList()
    private var loggedUserId: String = ""

    inner class ViewHolder(private val binding: ItemChatHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            binding.apply {
//                val latestTimestamp: Long? =
//                    conversation.messages.maxByOrNull { it.value.timestamp }?.value?.timestamp
//                val latestMessage: ChatMessage? =
//                    conversation.messages.values.first { it.timestamp == latestTimestamp }

                val latestMessage: ChatMessage? = conversation.lastMessage
//                val latestMessage: ChatMessage? = ChatMessage()

                setTimestamp(tvTimestamp, latestMessage?.timestamp ?: 0)

                tvMessage.text = latestMessage?.message

                Log.e("CHA", "type: ${conversation.conversationType}")
                when (conversation.conversationType) {
                    "GROUP" -> {
                        tvUserName.text = conversation.conversationName
                        setTimestamp(tvTimestamp, conversation.createdAt ?: 0)
                        Glide.with(itemView.context)
                            .load(R.drawable.ic_default_group_avatar).into(binding.ivUserAvatar)
                        if (conversation.conversationAvatar != null && conversation.conversationAvatar != "") {
                            Glide.with(itemView.context)
                                .load(conversation.conversationAvatar).into(binding.ivUserAvatar)
                        } else {
                            Log.e("CHA", "else")
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_default_group_avatar).into(binding.ivUserAvatar)
                        }

                        if (latestMessage?.messageId == null || latestMessage.messageId == ""){
                            if (conversation.createdBy == loggedUserId){
                                tvMessage.text = "You created this group"
                            } else {
                                tvMessage.text = "Someone added you"
                            }
                        }
                    }
                    "PERSONAL" -> {
                        tvUserName.text = conversation.interlocutor?.name

                        if (conversation.interlocutor?.avatarUrl != null && conversation.interlocutor?.avatarUrl != "") {
                            Glide.with(itemView.context)
                                .load(conversation.interlocutor?.avatarUrl)
                                .into(binding.ivUserAvatar)
                        } else {
                            Glide.with(itemView.context)
                                .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                        }

                        if (latestMessage?.senderId == loggedUserId) {
                            // the logged user is the sender
                            tvMessage.text = "you: ${latestMessage.message}"

                            if (latestMessage.beenRead) {
                                tvMessageStatus.visibility = View.VISIBLE
                                tvMessageStatus.text = "✓✓"
                                tvMessageStatus.setTextColor(
                                    ResourcesCompat.getColor(
                                        itemView.resources,
                                        R.color.message_been_read_color,
                                        null
                                    )
                                )
                            } else {
                                // if hasn't been read'
                                if (latestMessage.deliveredTimestamp != 0L) {
                                    tvMessageStatus.visibility = View.VISIBLE
                                    tvMessageStatus.text = "✓✓"
                                }

                                if (latestMessage.deliveredTimestamp == 0L && latestMessage.timestamp != 0L) {
                                    tvMessageStatus.visibility = View.VISIBLE
                                    tvMessageStatus.text = "✓"
                                }

                                // update the color
                                when (itemView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                                    Configuration.UI_MODE_NIGHT_YES -> {
                                        // The system is currently in night mode
                                        tvMessageStatus.setTextColor(
                                            ResourcesCompat.getColor(
                                                itemView.resources,
                                                R.color.night_theme_text_color,
                                                null
                                            )
                                        )
                                    }
                                    Configuration.UI_MODE_NIGHT_NO -> {
                                        // The system is currently in day mode
                                        tvMessageStatus.setTextColor(
                                            ResourcesCompat.getColor(
                                                itemView.resources,
                                                R.color.light_theme_text_color,
                                                null
                                            )
                                        )
                                    }
                                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                        // We don't know what mode we're in, assume day mode
                                        tvMessageStatus.setTextColor(
                                            ResourcesCompat.getColor(
                                                itemView.resources,
                                                R.color.light_theme_text_color,
                                                null
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            // the interlocutor is the sender and the logged user is the receiver
                            tvMessageStatus.visibility = View.GONE
                            if (latestMessage?.beenRead != true && conversation.unreadMessages > 0) {
                                // if there are unread messages
                                tvUnreadMessages.visibility = View.VISIBLE
                                tvUnreadMessages.text = conversation.unreadMessages.toString()
                            } else {
                                tvUnreadMessages.visibility = View.GONE
                            }
                        }
                    }
                }
            }
            itemView.setOnClickListener {
                when(conversation.conversationType){
                    "PERSONAL" -> {
                        itemView.context.startActivity(
                            Intent(
                                itemView.context,
                                PersonalChatActivity::class.java
                            ).putExtra(
                                PersonalChatActivity.EXTRA_INTENT_CONVERSATION_ID,
                                conversation.conversationId
                            ).putExtra(
                                PersonalChatActivity.EXTRA_INTENT_INTERLOCUTOR_ID,
                                conversation.interlocutor?.userId
                            )
                        )
                    }
                    "GROUP" -> {
                        Log.e("CHA", "go to group chat activity")
                        itemView.context.startActivity(
                            Intent(
                                itemView.context,
                                GroupChatActivity::class.java
                            ).putExtra(
                                GroupChatActivity.EXTRA_INTENT_CONVERSATION_ID,
                                conversation.conversationId
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemChatHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    private fun setTimestamp(tvTimestamp: TextView, timestamp: Long){
        val todayDate = DateHelper.getCurrentDate()
        val startDate = DateHelper.getThisWeekStartDate()
        val endDate = DateHelper.getThisWeekEndDate()
        val timestampDate = DateHelper.getDateFromTimestamp(timestamp ?: 0)

        if (todayDate.equals(timestampDate, ignoreCase = true)) { // today
            tvTimestamp.text =
                DateHelper.getTimeFromTimestamp(timestamp ?: 0)
        } else if (DateHelper.isYesterdayDate(timestamp ?: 0)) {
            tvTimestamp.text = "Yesterday"
        } else if (timestampDate in startDate..endDate) { // still this week
            tvTimestamp.text =
                DateHelper.getDayNameFromTimestamp(timestamp ?: 0)
        } else {
            tvTimestamp.text =
                DateHelper.getFormattedDateFromTimestamp(timestamp ?: 0)
        }
    }

    fun setLoggedUserId(userId: String) {
        loggedUserId = userId
    }

    fun setData(newList: List<Conversation>) {
        conversations.clear()
        conversations.addAll(newList)
        notifyDataSetChanged()
    }
}