package com.mikirinkode.firebasechatapp.feature.chat.chatroom

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper
import com.mikirinkode.firebasechatapp.constants.ConversationType
import com.mikirinkode.firebasechatapp.constants.MessageType
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.Conversation
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ItemMessageBinding
import java.util.*

class ConversationAdapter : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    private var conversation: Conversation? = null
    private val messages: ArrayList<ChatMessage> = ArrayList()
    private val participants: ArrayList<UserAccount> = ArrayList()
    private var loggedUserId: String = ""
    private var conversationType: String = ""

    private val listIndexOfSelectedMessages = ArrayList<Int>()
    private var currentSelectedMessage: ChatMessage? = null

    var chatClickListener: ChatClickListener? = null

    inner class ViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessage, position: Int) {
            binding.apply {

                // handle message type
                when (chat.type.uppercase()) {
                    MessageType.TEXT.toString() -> {
                        ivInterlocutorExtraImage.visibility = View.GONE
                        ivloggedUserExtraImage.visibility = View.GONE
                    }
                    MessageType.IMAGE.toString() -> {
                        if (chat.imageUrl != "") {
                            if (chat.senderId == loggedUserId) {
                                ivloggedUserExtraImage.visibility = View.VISIBLE
                                Glide.with(itemView.context)
                                    .load(chat.imageUrl)
                                    .placeholder(R.drawable.progress_animation)
                                    .into(ivloggedUserExtraImage)

                            } else {
                                ivInterlocutorExtraImage.visibility = View.VISIBLE
                                Glide.with(itemView.context)
                                    .load(chat.imageUrl)
                                    .into(ivInterlocutorExtraImage)
                            }
                        }
                    }
                    MessageType.VIDEO.toString() -> {}
                    MessageType.AUDIO.toString() -> {}
                }

                // show message
                if (chat.senderId == loggedUserId) {
                    layoutLoggedUserMessage.visibility = View.VISIBLE
                    layoutInterlocutorMessage.visibility = View.GONE

                    tvloggedUserMessage.text = chat.message
                    tvloggedUserTimestamp.text =
                        DateHelper.getTimeFromTimestamp(chat.sendTimestamp)

                    // if the logged user is the sender
                    // then show the message read status
                    updateMessageStatus(chat, tvloggedUserMessageStatus, itemView.resources)
                } else {
                    layoutLoggedUserMessage.visibility = View.GONE
                    layoutInterlocutorMessage.visibility = View.VISIBLE

                    tvInterlocutorMessage.text = chat.message
                    tvInterlocutorTimestamp.text =
                        DateHelper.getTimeFromTimestamp(chat.sendTimestamp)

                    when (conversationType) {
                        ConversationType.GROUP.toString() -> {
                            tvInterlocutorName.visibility = View.VISIBLE
                            tvInterlocutorName.text = chat.senderName

                            val sender = participants.firstOrNull { it.userId == chat.senderId }

                            // if the conversation type is group
                            // then show the user avatar using initial name
                            cardInterlocutorAvatar.visibility = View.VISIBLE


                            if (sender?.avatarUrl.isNullOrBlank()){
                                ivInterlocutorAvatar.visibility = View.GONE
                                tvAvatarName.visibility = View.VISIBLE
                                val separateName = chat.senderName.split(" ")

                                val firstNameInitial = chat.senderName[0]
                                val lastNameInitial = separateName.last().getOrNull(0)

                                val initialName =
                                    if (lastNameInitial == null) firstNameInitial.toString() else "${firstNameInitial}${lastNameInitial}"
                                tvAvatarName.text = initialName
                            } else {
                                tvAvatarName.visibility = View.GONE
                                ivInterlocutorAvatar.visibility = View.VISIBLE
                                Glide.with(itemView.context)
                                    .load(sender?.avatarUrl)
                                    .into(ivInterlocutorAvatar)
                            }

                        }
                        else -> {
                            cardInterlocutorAvatar.visibility = View.GONE
                            tvInterlocutorName.visibility = View.GONE
                        }
                    }
                }

                if (conversation != null){
                    val totalUnreadMessage = conversation!!.unreadMessageEachParticipant[loggedUserId]

                    if (totalUnreadMessage != null){

                        if (position == messages.size - totalUnreadMessage){
                            cardUnreadHeader.visibility = View.VISIBLE
                            tvUnreadHeader.text = "$totalUnreadMessage Unread Messages"
                        } else {
                            cardUnreadHeader.visibility = View.GONE
                        }
                    }
                } else {
                    cardUnreadHeader.visibility = View.GONE
                }


                // handle date header
                setupDateHeader(binding, position, itemView.context)

                // Handle on click events
                onCLickAction(binding, chat, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position], position)
    }

    private fun setupDateHeader(binding: ItemMessageBinding, position: Int, context: Context){
        binding.apply {
            val message = messages[position]

            var headerTimestamp: Long? = null

            if (position == 0) {
                headerTimestamp = message.sendTimestamp
            }

            if (position > 1 && position + 1 < messages.size - 1) {
                val prevMessage = messages[position - 1]

                val calendar = GregorianCalendar.getInstance()

                calendar.time = DateHelper.formatTimestampToDate(message.sendTimestamp)
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                calendar.time = DateHelper.formatTimestampToDate(prevMessage.sendTimestamp)
                val prevDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                // if day in this message and day in previous message is different
                // then show the header
                if (prevDayOfYear != dayOfYear) {
                    headerTimestamp = message.sendTimestamp
                }
            }
            if (headerTimestamp != null) {
                cardDateHeader.visibility = View.VISIBLE

                val todayDate = DateHelper.getCurrentDate()
                val startDate = DateHelper.getThisWeekStartDate()
                val endDate = DateHelper.getThisWeekEndDate()
                val timestampDate = DateHelper.getDateFromTimestamp(headerTimestamp)

                if (todayDate.equals(timestampDate, ignoreCase = true)) { // today
                    tvDateHeader.text =
                        context.getString(R.string.txt_today)
                } else if (DateHelper.isYesterdayDate(headerTimestamp)) { // yesterday
                    tvDateHeader.text =
                        context.getString(R.string.txt_yesterday)
                } else if (timestampDate in startDate..endDate) { // still in this week
                    tvDateHeader.text =
                        DateHelper.getDayNameFromTimestamp(headerTimestamp)
                } else {
                    tvDateHeader.text = DateHelper.regularFormat(headerTimestamp)
                }
            } else {
                cardDateHeader.visibility = View.GONE
            }
        }
    }

    private fun updateMessageStatus(
        chat: ChatMessage,
        tvloggedUserMessageStatus: TextView,
        resources: Resources
    ) {
        // check if message is been read by all user or not yet
        val isBeenRead = chat.beenReadBy.size == participants.size - 1
        if (isBeenRead) {
            tvloggedUserMessageStatus.visibility = View.VISIBLE
            tvloggedUserMessageStatus.text = "✓✓"
            tvloggedUserMessageStatus.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.message_been_read_color,
                    null
                )
            )
        } else {
            // if chat hasn't been read
            // TODO delivered timestamp
            val isDelivered = chat.beenDeliveredTo.isNotEmpty()
            if (isDelivered){
                tvloggedUserMessageStatus.visibility = View.VISIBLE
                tvloggedUserMessageStatus.text = "✓✓"
            } else if (chat.sendTimestamp != 0L){
                tvloggedUserMessageStatus.visibility = View.VISIBLE
                tvloggedUserMessageStatus.text = "✓"
            }

            // update the status color
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    // The system is currently in night mode
                    tvloggedUserMessageStatus.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.night_theme_text_color,
                            null
                        )
                    )
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    // The system is currently in day mode
                    tvloggedUserMessageStatus.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.light_theme_text_color,
                            null
                        )
                    )
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    // We don't know what mode we're in, assume day mode
                    tvloggedUserMessageStatus.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.light_theme_text_color,
                            null
                        )
                    )
                }
            }
        }
    }

    private fun onCLickAction(binding: ItemMessageBinding, chat: ChatMessage, position: Int){
        binding.apply {
            // update selected status
            if (chat.isSelected) {
                if (chat.senderId == loggedUserId) {
                    layoutLoggedUserOnSelected.visibility = View.VISIBLE
                } else {
                    layoutIntercolucatorOnSelected.visibility = View.VISIBLE
                }
            } else {
                if (chat.senderId == loggedUserId) {
                    layoutLoggedUserOnSelected.visibility = View.GONE
                } else {
                    layoutIntercolucatorOnSelected.visibility = View.GONE
                }
            }

            /**
             * Interlocutor On click Listener
             */
            ivInterlocutorExtraImage.setOnClickListener {
                chatClickListener?.onImageClick(chat)
            }
            layoutInterlocutorMessage.setOnClickListener {
                if (chat.isSelected) {
                    chat.isSelected = false
                    listIndexOfSelectedMessages.remove(position)
                    notifyItemChanged(position)
                    chatClickListener?.onMessageDeselect()
                }
            }
            layoutInterlocutorMessage.setOnLongClickListener {
                if (!chat.isSelected) {
                    chat.isSelected = true
                    currentSelectedMessage = chat
                    notifyItemChanged(position)
                    listIndexOfSelectedMessages.add(position)
                    chatClickListener?.onMessageSelected()
                }
                true
            }


            /**
             * Logged User On Click Listener
             */
            ivloggedUserExtraImage.setOnClickListener {
                chatClickListener?.onImageClick(chat)
            }

            layoutLoggedUserMessage.setOnClickListener {
                if (chat.isSelected) {
                    chat.isSelected = false
                    listIndexOfSelectedMessages.remove(position)
                    notifyItemChanged(position)
                    chatClickListener?.onMessageDeselect()
                }
            }
            layoutLoggedUserMessage.setOnLongClickListener {
                if (!chat.isSelected) {
                    chat.isSelected = true
                    currentSelectedMessage = chat
                    listIndexOfSelectedMessages.add(position)
                    notifyItemChanged(position)
                    chatClickListener?.onMessageSelected()
                }
                true
            }
        }
    }

    fun getTotalUnreadMessageLoggedUser(): Int {
        return conversation?.unreadMessageEachParticipant?.get(loggedUserId) ?: 0
    }

    fun setLoggedUserId(userId: String) {
        loggedUserId = userId
    }

    fun setConversationType(type: String) {
        conversationType = type
    }

    fun setConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    fun getConversation() = conversation

    fun setMessages(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    fun setParticipants(newList: List<UserAccount>) {
        participants.clear()
        participants.addAll(newList)
        notifyDataSetChanged()
    }

    fun getParticipants() = participants

    fun onDeselectAllMessage() {
        listIndexOfSelectedMessages.forEach { index ->
            val item: ChatMessage? = messages[index]
            if (item != null) {
                item.isSelected = false
                notifyItemChanged(index)
            }
        }
        listIndexOfSelectedMessages.clear()
    }

    fun getReceiverDeviceToken(): ArrayList<String> {
        val tokenList = ArrayList<String>()
        for (user in participants){
            if (user.oneSignalToken != null && user.userId != loggedUserId){
                tokenList.add(user.oneSignalToken)
            }
        }
        return tokenList
    }

    fun getTotalSelectedMessages() = listIndexOfSelectedMessages.size

    fun getCurrentSelectedMessage() = currentSelectedMessage

    fun isChatEmpty() = messages.isEmpty()

    interface ChatClickListener {

        fun onImageClick(chat: ChatMessage)

        fun onMessageSelected()
        fun onMessageDeselect()
    }
}