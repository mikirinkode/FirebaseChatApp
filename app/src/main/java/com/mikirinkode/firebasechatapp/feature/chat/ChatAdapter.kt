package com.mikirinkode.firebasechatapp.feature.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.databinding.ItemMessageBinding
import com.mikirinkode.firebasechatapp.helper.DateHelper
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messages: ArrayList<ChatMessage> = ArrayList()
    private var loggedUserId: String = ""
    var chatClickListener: ChatClickListener? = null

    private val dateType = 0
    private val messageType = 1

    inner class ViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.apply {

                if (chat.senderId == loggedUserId) {
                    loggedUserMessageCard.visibility = View.VISIBLE
                    interlocutorMessageCard.visibility = View.GONE

                    tvloggedUserMessage.text = chat.message
                    tvloggedUserTimestamp.text = DateHelper.messageTimeFormat(chat.timestamp)

                    if (chat.imageUrl != "") {
                        ivloggedUserExtraImage.visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(chat.imageUrl)
                            .into(ivloggedUserExtraImage)
                    }

                    if (chat.beenRead){
                        tvloggedUserMessageStatus.visibility = View.VISIBLE
                        tvloggedUserMessageStatus.text = "✓✓"
                        tvloggedUserMessageStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.message_been_read_color, null))
                    }
                    if (!chat.beenRead && chat.deliveredTimestamp != 0L){
                        tvloggedUserMessageStatus.visibility = View.VISIBLE
                        tvloggedUserMessageStatus.text = "✓✓"
                    }

                    if (!chat.beenRead && chat.deliveredTimestamp == 0L && chat.timestamp != 0L){
                        tvloggedUserMessageStatus.visibility = View.VISIBLE
                        tvloggedUserMessageStatus.text = "✓"
                    }

                    if (!chat.beenRead && chat.deliveredTimestamp == 0L && chat.timestamp == 0L){
                        tvloggedUserMessageStatus.visibility = View.VISIBLE
                        tvloggedUserMessageStatus.text = "sending"
                    }

                } else {
                    loggedUserMessageCard.visibility = View.GONE
                    interlocutorMessageCard.visibility = View.VISIBLE

                    tvInterlocutorMessage.text = chat.message
                    tvInterlocutorTimestamp.text = DateHelper.messageTimeFormat(chat.timestamp)

                    if (chat.imageUrl != "") {
                        ivInterlocutorExtraImage.visibility = View.VISIBLE
                        Glide.with(itemView.context)
                            .load(chat.imageUrl)
                            .into(ivInterlocutorExtraImage)
                    }
                }

                itemView.setOnLongClickListener {
                    chatClickListener?.onLongClick(chat)
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

        if (position == 0){
            headerTimestamp = message.timestamp
        }

        if (position > 1 && position + 1 < messages.size -1){
            val prevMessage = messages[position -1]

            val calendar = GregorianCalendar.getInstance()
            
            calendar.time = DateHelper.formatTimestampToDate(message.timestamp)
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            
            calendar.time = DateHelper.formatTimestampToDate(prevMessage.timestamp)
            val prevDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

            // if day in this message and day in previous message is different
            // then show the header
            if (prevDayOfYear != dayOfYear){
                headerTimestamp = message.timestamp
            }
        }
        if (headerTimestamp != null){
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

    fun setLoggedUserId(userId: String){
        loggedUserId = userId
    }

    fun setData(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    interface ChatClickListener {
        fun onLongClick(chat: ChatMessage)
    }
}