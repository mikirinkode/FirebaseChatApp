package com.mikirinkode.firebasechatapp.feature.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.mikirinkode.firebasechatapp.MainActivity
import com.mikirinkode.firebasechatapp.databinding.ActivityLoginBinding
import com.mikirinkode.firebasechatapp.feature.register.RegisterActivity

class LoginActivity : AppCompatActivity(), LoginView {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        setupPresenter()
        actionClicked()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun initView() {}
    private fun setupPresenter(){
        presenter = LoginPresenter()
        presenter.attachView(this)
    }

    private fun goToMainView(){
        startActivity(Intent(this, MainActivity::class.java))
    }
    override fun onLoginSuccess() {
        Log.e("LoginActivity", "login onLoginSuccess")
        goToMainView()
    }

    override fun onLoginFailed(message: String) {
        Log.e("LoginActivity", "login onLoginFailed")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
//        TODO("Not yet implemented")
    }

    override fun hideLoading() {
//        TODO("Not yet implemented")
    }

    private fun actionClicked() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email: String = etEmail.text.toString().trim()
                val password: String = etPassword.text.toString().trim()
                Log.e("LoginActivity", "login onclick")
                presenter.performSignIn(email, password)
            }

            btnGoToRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        }
    }
}