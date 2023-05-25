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
    var chatClickListener: ChatClickListener? = null

    inner class ViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
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

                /**
                 * Interlocutor On click Listener
                 */
                ivInterlocutorExtraImage.setOnClickListener {
                    chatClickListener?.onImageClick(chat)
                }
                layoutInterlocutorMessage.setOnClickListener {
                    if (layoutIntercolucatorOnSelected.visibility == View.VISIBLE) {
                        layoutIntercolucatorOnSelected.visibility = View.GONE
                        chatClickListener?.onMessageDeselect()
                    }

                }
                layoutInterlocutorMessage.setOnLongClickListener {
                    if (layoutIntercolucatorOnSelected.visibility == View.GONE) {
                        layoutIntercolucatorOnSelected.visibility = View.VISIBLE
                        chatClickListener?.onLongClick(chat)
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
                    if (layoutLoggedUserOnSelected.visibility == View.VISIBLE) {
                        layoutLoggedUserOnSelected.visibility = View.GONE
                        chatClickListener?.onMessageDeselect()
                    }
                }
                layoutLoggedUserMessage.setOnLongClickListener {
                    if (layoutLoggedUserOnSelected.visibility == View.GONE) {
                        layoutLoggedUserOnSelected.visibility = View.VISIBLE
                        chatClickListener?.onMessageSelected()
                        chatClickListener?.onLongClick(chat)
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
        holder.bind(messages[position])
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
            holder.binding.tvDateHeader.text = DateHelper.regularFormat(headerTimestamp)
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

    interface ChatClickListener {
        fun onLongClick(chat: ChatMessage)
        fun onImageClick(chat: ChatMessage)

        fun onMessageSelected()
        fun onMessageDeselect()
    }
}