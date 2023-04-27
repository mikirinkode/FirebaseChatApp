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
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val messages: ArrayList<ChatMessage> = ArrayList()
    private var loggedUserId: String = ""
    var chatClickListener: ChatClickListener? = null

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

                if (chat.imageUrl != ""){
                    ivMessageExtraImage.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(chat.imageUrl)
                        .into(ivMessageExtraImage)
                }

                val view = itemView.findViewById<View>(R.id.cardItemMessage)
                val params = view.layoutParams as ViewGroup.MarginLayoutParams

                if(chat.senderId == loggedUserId) {

                    params.setMargins(192, 32, 32, 0)
                    view.layoutParams = params
                    view.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_sender_message_card)
//                    view.setBackgroundResource(R.drawable.bg_sender_message_card)

                    if (chat.beenRead){
                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "✓✓"
                        tvMessageStatus.setTextColor(ResourcesCompat.getColor(itemView.resources, R.color.message_been_read_color, null))
                    }
                    if (!chat.beenRead && chat.deliveredTimestamp != 0L){
                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "✓✓"
                    }

                    if (!chat.beenRead && chat.deliveredTimestamp == 0L && chat.timestamp != 0L){
                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "✓"
                    }

                    if (!chat.beenRead && chat.deliveredTimestamp == 0L && chat.timestamp == 0L){
                        tvMessageStatus.visibility = View.VISIBLE
                        tvMessageStatus.text = "sending"
                    }

                } else {
                    tvMessageStatus.visibility = View.GONE
//                    ivMessageStatus.visibility = View.GONE
//                    ivBeenRead.visibility = View.GONE
//                    ivUnread.visibility = View.GONE

                    params.setMargins(32, 32, 128, 0)
                    view.layoutParams = params
                    view.background = ContextCompat.getDrawable(itemView.context, R.drawable.bg_receiver_message_card)
//                    view.setBackgroundResource(R.drawable.bg_receiver_message_card)
                }
            }

            itemView.setOnLongClickListener {
                chatClickListener?.onLongClick(chat)
                true
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

    interface ChatClickListener {
        fun onLongClick(chat: ChatMessage)
    }
}