package com.mikirinkode.firebasechatapp.feature.selectuser

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ItemSelectedUserBinding

class SelectedUserAdapter : RecyclerView.Adapter<SelectedUserAdapter.ViewHolder>() {

    private val userList: ArrayList<UserAccount> = ArrayList()

    var userClickListener: UserClickListener? = null

    inner class ViewHolder(private val binding: ItemSelectedUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserAccount) {
            binding.apply {
                tvUserName.text = user.name

                if (user.avatarUrl != null && user.avatarUrl != "") {
                    Glide.with(itemView.context)
                        .load(user.avatarUrl).into(binding.ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                }
            }
            itemView.setOnClickListener {
                userClickListener?.onRemoveSelected(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    interface UserClickListener {
        fun onRemoveSelected(user: UserAccount)
    }
}