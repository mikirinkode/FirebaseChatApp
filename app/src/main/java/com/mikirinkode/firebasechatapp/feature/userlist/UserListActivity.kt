package com.mikirinkode.firebasechatapp.feature.userlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.mikirinkode.firebasechatapp.databinding.ActivityUserListBinding

class UserListActivity : AppCompatActivity(), UserListView {

    private val binding: ActivityUserListBinding by lazy {
        ActivityUserListBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: UserListPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        setupPresenter()
        onActionClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun initView() {}

    private fun setupPresenter(){
        presenter = UserListPresenter()
        presenter.attachView(this)
        presenter.getUserList()
    }

    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun onActionClicked() {
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}