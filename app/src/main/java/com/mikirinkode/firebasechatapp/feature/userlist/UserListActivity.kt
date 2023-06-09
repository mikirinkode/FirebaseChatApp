package com.mikirinkode.firebasechatapp.feature.userlist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ActivityUserListBinding
import com.mikirinkode.firebasechatapp.feature.chat.PersonalChatActivity
import com.mikirinkode.firebasechatapp.feature.group.CreateGroupChatActivity

class UserListActivity : AppCompatActivity(), UserListView, UserListAdapter.UserClickListener {

    private val binding: ActivityUserListBinding by lazy {
        ActivityUserListBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: UserListPresenter

    private val userAdapter: UserListAdapter by lazy {
        UserListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initAdapter()
        initView()
        setupPresenter()
        onActionClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun initAdapter(){
        userAdapter.userClickListener = this@UserListActivity
    }

    private fun initView() {
        binding.apply {
            rvUser.layoutManager = LinearLayoutManager(this@UserListActivity)
            rvUser.adapter = userAdapter
        }
    }

    private fun setupPresenter() {
        presenter = UserListPresenter()
        presenter.attachView(this)
        presenter.getUserList()
    }

    override fun setDataToRecyclerView(users: List<UserAccount>) {
        userAdapter.setData(users)
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    override fun onUserClick(user: UserAccount) {
        // TODO:
        startActivity(
            Intent(this, PersonalChatActivity::class.java)
                .putExtra(PersonalChatActivity.EXTRA_INTENT_INTERLOCUTOR_ID, user.userId)
        )
    }

    private fun onActionClicked() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnNewGroup.setOnClickListener {
            startActivity(Intent(this, CreateGroupChatActivity::class.java))
        }
    }
}