package com.mikirinkode.firebasechatapp.firebase.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper

class EmailLoginHelper(
    private val mListener: EmailLoginListener,
) {

    private val auth: FirebaseAuth? = FirebaseHelper.instance().getFirebaseAuth()
    private val pref = LocalSharedPref.instance()

    fun performLogin(
        email: String,
        password: String
    ) {
        Log.e("EmailSignInHelper", "login performSignIn")
        Log.e("EmailSignInHelper", "login auth: $auth")
        if (loginValidator(email, password)) {
            if (auth == null){
                mListener.onEmailLoginFail("An Error Occurred, please report to the developer.")
            }
            auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    mListener.onEmailLoginSuccess(user?.uid)

                    val username = user?.displayName ?: user?.email ?: "user"
                    pref?.startSession(username)

                } else {
                    Log.e(TAG, task.exception.toString())
                    mListener.onEmailLoginFail("Authentication failed")
                    Log.e("EmailSignInHelper", "login Authentication failed")
                }
            }
        } else {
            mListener.onEmailLoginFail("email or password is invalid.")
            Log.e("EmailSignInHelper", "login email or password is invalid")
        }
    }

    private fun loginValidator(email: String, password: String): Boolean {
        return !(email.isEmpty() || password.isEmpty())
    }

    companion object {
        private const val TAG = "EmailSignInHelper"
    }
}