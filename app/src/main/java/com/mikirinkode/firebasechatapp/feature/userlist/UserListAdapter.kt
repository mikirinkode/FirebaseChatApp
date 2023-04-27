package com.mikirinkode.firebasechatapp.feature.userlist

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ItemUserBinding
import com.mikirinkode.firebasechatapp.feature.chat.ChatActivity

class UserListAdapter : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    private val userList: ArrayList<UserAccount> = ArrayList()

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserAccount) {
            binding.apply {
                tvUserName.text = user.name

                if (user.avatarUrl != null && user.avatarUrl != "") {
                    Glide.with(itemView.context)
                        .load(user.avatarUrl).into(binding.ivUserAvatar)
                }
            }
            itemView.setOnClickListener {
                itemView.context.startActivity(
                    Intent(
                        itemView.context,
                        ChatActivity::class.java
                    )
                        .putExtra(ChatActivity.EXTRA_INTENT_OPENED_USER_ID, user.userId)
                        .putExtra(ChatActivity.EXTRA_INTENT_OPENED_USER_AVATAR, user.avatarUrl)
                        .putExtra(ChatActivity.EXTRA_INTENT_OPENED_USER_NAME, user.name)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    fun setData(newList: List<UserAccount>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}