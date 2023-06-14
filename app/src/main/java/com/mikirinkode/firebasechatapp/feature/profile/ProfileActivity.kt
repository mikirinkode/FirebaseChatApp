package com.mikirinkode.firebasechatapp.feature.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mikirinkode.firebasechatapp.R
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.databinding.ActivityProfileBinding
import com.mikirinkode.firebasechatapp.feature.login.LoginActivity

// TODO
class ProfileActivity : AppCompatActivity(), ProfileView {

    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    private lateinit var presenter: ProfilePresenter

    companion object {
        const val EXTRA_INTENT_USER_ID = "key_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        setupPresenter()
        handleIntent()
        onActionClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun initView() {}

    private fun handleIntent(){
        val userId = intent.getStringExtra(EXTRA_INTENT_USER_ID)
        if (userId != null){
            presenter.observeUserProfile(userId)
        }
    }

    private fun setupPresenter() {
        presenter = ProfilePresenter()
        presenter.attachView(this)
    }

    override fun onGetProfileSuccess(user: UserAccount) {
        binding.apply {
            tvUserEmail.text = user.email
            tvUserFullName.text = user.name

            if (user.avatarUrl != null && user.avatarUrl != "") {
                Glide.with(this@ProfileActivity)
                    .load(user.avatarUrl).into(ivAvatar)
            }
        }
    }

    override fun showLoading() {}
    override fun hideLoading() {}

    private fun onActionClicked() {
        binding.apply {
            btnBack.setOnClickListener {
                onBackPressed()
            }

            btnLogout.setOnClickListener {
                pref?.clearSession()
                Firebase.auth.signOut()
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                finishAffinity()
            }
        }
    }
}