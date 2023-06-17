package com.mikirinkode.firebasechatapp.feature.selectuser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ItemSelectUserBinding

class SelectUserAdapter : RecyclerView.Adapter<SelectUserAdapter.ViewHolder>() {

    private val userList: ArrayList<UserAccount> = ArrayList()

    var userClickListener: UserClickListener? = null

    inner class ViewHolder(private val binding: ItemSelectUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserAccount, position: Int) {
            binding.apply {
                tvUserName.text = user.name

                if (user.avatarUrl != null && user.avatarUrl != "") {
                    Glide.with(itemView.context)
                        .load(user.avatarUrl).into(binding.ivUserAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(R.drawable.ic_default_user_avatar).into(binding.ivUserAvatar)
                }

                if (user.isSelected) {
                    ivSelected.visibility = View.VISIBLE
                } else {
                    ivSelected.visibility = View.GONE
                }
            }
            itemView.setOnClickListener {
                user.isSelected = !user.isSelected
                notifyItemChanged(position)
                userClickListener?.onUserClick(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSelectUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userList[position], position)
    }

    fun setData(newList: List<UserAccount>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    interface UserClickListener {
        fun onUserClick(user: UserAccount)
    }
}