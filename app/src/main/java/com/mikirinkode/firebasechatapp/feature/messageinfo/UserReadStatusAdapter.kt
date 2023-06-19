package com.mikirinkode.firebasechatapp.feature.messageinfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper
import com.mikirinkode.firebasechatapp.data.model.ChatMessage
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ItemMessageReadByBinding

class UserReadStatusAdapter: RecyclerView.Adapter<UserReadStatusAdapter.ViewHolder>() {

    private val userList: ArrayList<UserAccount> = ArrayList()
    private var message: ChatMessage? = null

    inner class ViewHolder(private val binding: ItemMessageReadByBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserAccount) {
            binding.apply {
                tvUserName.text = user.name
                val timeStamp = message?.beenReadBy?.get(user.userId)

                if (timeStamp == null || timeStamp == 0L){
                    tvReadTimestamp.text = "-"
                } else {
                    tvReadTimestamp.text = DateHelper.getRegularFormattedDateTimeFromTimestamp(timeStamp)
                }

                if (user.avatarUrl != null && user.avatarUrl != "") {
                    Glide.with(itemView.context)
                        .load(user.avatarUrl).into(binding.ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserReadStatusAdapter.ViewHolder {
        val binding = ItemMessageReadByBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserReadStatusAdapter.ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    fun setUserList(newList: List<UserAccount>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    fun setMessage(newMessage: ChatMessage) {
        this.message = newMessage
    }
}