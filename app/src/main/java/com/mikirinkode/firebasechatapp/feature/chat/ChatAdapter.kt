package com.mikirinkode.firebasechatapp.feature.chat

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.databinding.ItemMessageBinding
import com.mikirinkode.firebasechatapp.helper.DateHelper
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messages: ArrayList<ChatMessage> = ArrayList()
    private var loggedUserId: String = ""

    private val listIndexOfSelectedMessages = ArrayList<Int>()
    private var currentSelectedMessage: ChatMessage? = null

    var chatClickListener: ChatClickListener? = null

    inner class ViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatMessage, position: Int) {
            binding.apply {
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
                        DateHelper.getTimeFromTimestamp(chat.timestamp)
                } else {
                    layoutLoggedUserMessage.visibility = View.GONE
                    layoutInterlocutorMessage.visibility = View.VISIBLE

                    tvInterlocutorMessage.text = chat.message
                    tvInterlocutorTimestamp.text = DateHelper.getTimeFromTimestamp(chat.timestamp)
                }

                // update message status
                if (chat.senderId == loggedUserId) {
                    if (chat.beenRead) {
                        tvloggedUserMessageStatus.visibility = View.VISIBLE
                        tvloggedUserMessageStatus.text = "✓✓"
                        tvloggedUserMessageStatus.setTextColor(
                            ResourcesCompat.getColor(
                                itemView.resources,
                                R.color.message_been_read_color,
                                null
                            )
                        )
                    } else {
                        // if chat hasn't been read
                        if (chat.deliveredTimestamp != 0L) {
                            tvloggedUserMessageStatus.visibility = View.VISIBLE
                            tvloggedUserMessageStatus.text = "✓✓"
                        }

                        if (chat.deliveredTimestamp == 0L && chat.timestamp != 0L) {
                            tvloggedUserMessageStatus.visibility = View.VISIBLE
                            tvloggedUserMessageStatus.text = "✓"
                        }

                        if (chat.deliveredTimestamp == 0L && chat.timestamp == 0L) {
                            tvloggedUserMessageStatus.visibility = View.VISIBLE
                            tvloggedUserMessageStatus.text = "sending"
                        }

                        // update the status color
                        when (itemView.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                // The system is currently in night mode
                                tvloggedUserMessageStatus.setTextColor(
                                    ResourcesCompat.getColor(
                                        itemView.resources,
                                        R.color.night_theme_text_color,
                                        null
                                    )
                                )
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                // The system is currently in day mode
                                tvloggedUserMessageStatus.setTextColor(
                                    ResourcesCompat.getColor(
                                        itemView.resources,
                                        R.color.light_theme_text_color,
                                        null
                                    )
                                )
                            }
                            Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                                // We don't know what mode we're in, assume day mode
                                tvloggedUserMessageStatus.setTextColor(
                                    ResourcesCompat.getColor(
                                        itemView.resources,
                                        R.color.light_theme_text_color,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }


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
                layoutInterlocutorMessage.setOnClickListener {// TODO
                    if (chat.isSelected){
                        chat.isSelected = false
                        listIndexOfSelectedMessages.remove(position)
                        notifyItemChanged(position)
                        chatClickListener?.onMessageDeselect()
                    }
                }
                layoutInterlocutorMessage.setOnLongClickListener {// TODO
                    if (!chat.isSelected){
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

                layoutLoggedUserMessage.setOnClickListener {// TODO
                    if (chat.isSelected){
                        chat.isSelected = false
                        listIndexOfSelectedMessages.remove(position)
                        notifyItemChanged(position)
                        chatClickListener?.onMessageDeselect()
                    }
                }
                layoutLoggedUserMessage.setOnLongClickListener { // TODO
                    if (!chat.isSelected){
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
        val message = messages[position]

        var headerTimestamp: Long? = null

        if (position == 0) {
            headerTimestamp = message.timestamp
        }

        if (position > 1 && position + 1 < messages.size - 1) {
            val prevMessage = messages[position - 1]

            val calendar = GregorianCalendar.getInstance()

            calendar.time = DateHelper.formatTimestampToDate(message.timestamp)
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

            calendar.time = DateHelper.formatTimestampToDate(prevMessage.timestamp)
            val prevDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

            // if day in this message and day in previous message is different
            // then show the header
            if (prevDayOfYear != dayOfYear) {
                headerTimestamp = message.timestamp
            }
        }
        if (headerTimestamp != null) {
            holder.binding.cardDateHeader.visibility = View.VISIBLE
//            holder.binding.tvDateHeader.text = DateHelper.regularFormat(headerTimestamp)

            val todayDate = DateHelper.getCurrentDate()
            val startDate = DateHelper.getThisWeekStartDate()
            val endDate = DateHelper.getThisWeekEndDate()
            val timestampDate = DateHelper.getDateFromTimestamp(headerTimestamp ?: 0)

            if (todayDate.equals(timestampDate, ignoreCase = true)) { // today
                holder.binding.tvDateHeader.text = "Today"
            } else if (DateHelper.isYesterdayDate(headerTimestamp ?: 0)){
                holder.binding.tvDateHeader.text = "Yesterday"
            }
            else if (timestampDate in startDate..endDate) { // still this week
                holder.binding.tvDateHeader.text = DateHelper.getDayNameFromTimestamp(headerTimestamp ?: 0)
            } else {
                holder.binding.tvDateHeader.text = DateHelper.regularFormat(headerTimestamp ?: 0)
            }
        } else {
            holder.binding.cardDateHeader.visibility = View.GONE
        }

//        headerDate?.let {
//            holder.binding.tvDateHeader.visibility = View.VISIBLE
//            holder.binding.tvDateHeader.text = it.toString()
//        } ?: run {
//            holder.binding.tvDateHeader.visibility = View.GONE
//        }
    }

    fun setLoggedUserId(userId: String) {
        loggedUserId = userId
    }

    fun setData(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

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

    fun getTotalSelectedMessages() = listIndexOfSelectedMessages.size

    fun getCurrentSelectedMessage() = currentSelectedMessage

    fun isChatEmpty() = messages.isEmpty()

    interface ChatClickListener {

        fun onImageClick(chat: ChatMessage)

        fun onMessageSelected()
        fun onMessageDeselect()
    }
}