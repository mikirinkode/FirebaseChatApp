package com.mikirinkode.firebasechatapp.feature.login

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.km4quest.wafa.data.local.prefs.DataConstant
import com.mikirinkode.firebasechatapp.feature.main.MainActivity
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.databinding.ActivityLoginBinding
import com.mikirinkode.firebasechatapp.feature.register.RegisterActivity

class LoginActivity : AppCompatActivity(), LoginView {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var presenter: LoginPresenter
    private val pref: LocalSharedPref? by lazy {
        LocalSharedPref.instance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkLoggedUser()
        initView()
        setupPresenter()
        actionClicked()
    }

    override fun onResume() {
        super.onResume()
        presenter.updateUserOnlineStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }


    private fun checkLoggedUser() {
        val isLoggedIn: Boolean? = pref?.getBoolean(DataConstant.IS_LOGGED_IN)

        if (isLoggedIn == true) {
            goToMainView()
        }
    }


    private fun initView() {}
    private fun setupPresenter() {
        presenter = LoginPresenter()
        presenter.attachView(this)
        presenter.updateUserOnlineStatus()
    }

    private fun goToMainView() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
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
        binding.progressBarLogin.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.progressBarLogin.visibility = View.GONE
    }

    private fun actionClicked() {
        binding.apply {
            btnLogin.setOnClickListener {
                val email: String = etEmail.text.toString().trim()
                val password: String = etPassword.text.toString().trim()
                Log.e("LoginActivity", "login onclick")
                presenter.performSignIn(email, password)
            }

            btnGoogle.setOnClickListener {
                presenter.performSignInGoogle(this@LoginActivity)
            }

            btnGoToRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            presenter.onActivityResult(this, requestCode, resultCode, data)
        }
    }
}