package com.mikirinkode.firebasechatapp.feature.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.databinding.ItemMessageBinding
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messages: ArrayList<ChatMessage> = ArrayList()
    private var loggedUserId: String = ""

    inner class ViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatMessage) {
            binding.apply {
                // TODO: create date helper
                val timestamp = Timestamp(chat.timestamp)
                val date = Date(timestamp.time)
                val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val time = dateFormat.format(date)

                tvMessage.text = chat.message
                tvTimestamp.text = time

                val view = itemView.findViewById<View>(R.id.cardItemMessage)
                val params = view.layoutParams as ViewGroup.MarginLayoutParams

                if(chat.senderId == loggedUserId) {
                    params.setMargins(192, 32, 32, 0)
                    view.layoutParams = params
                    view.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_sender_message_card)
//                    view.setBackgroundResource(R.drawable.bg_sender_message_card)
                } else {
                    params.setMargins(32, 32, 128, 0)
                    view.layoutParams = params
                    view.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_receiver_message_card)
//                    view.setBackgroundResource(R.drawable.bg_receiver_message_card)
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
    }

    fun setLoggedUserId(userId: String){
        loggedUserId = userId
    }

    fun setData(newList: List<ChatMessage>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }
}