package com.mikirinkode.firebasechatapp.feature.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mikirinkode.firebasechatapp.MainActivity
import com.mikirinkode.firebasechatapp.databinding.ActivityRegisterBinding
import com.mikirinkode.firebasechatapp.feature.login.LoginActivity

class RegisterActivity : AppCompatActivity(), RegisterView {
    private val binding: ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: RegisterPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        setupPresenter()
        onActionClick()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun initView() {}

    private fun setupPresenter() {
        presenter = RegisterPresenter()
        presenter.attachView(this)
    }
    private fun goToMainView(){
        startActivity(Intent(this, MainActivity::class.java))
    }
    override fun onRegisterSuccess() {
        goToMainView()
    }

    override fun onRegisterFailed(message: String) {
//        TODO("Not yet implemented")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
//        TODO("Not yet implemented")
    }

    override fun hideLoading() {
//        TODO("Not yet implemented")
    }

    private fun onActionClick(){
        binding.apply {
            btnRegister.setOnClickListener {
                val email: String = etEmail.text.toString().trim()
                val password: String = etPassword.text.toString().trim()
                presenter.performRegister(email, password)
            }

            btnGoToLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}