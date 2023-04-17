package com.mikirinkode.firebasechatapp.firebase.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper

class EmailRegisterHelper(
    private val mListener: EmailRegisterListener
) {

    private val auth: FirebaseAuth? = FirebaseHelper.instance().getFirebaseAuth()


    fun performRegister(
        email: String,
        password: String,
    ) {
        Log.e("EmailRegisterHelper", "login perform register")
        if (loginValidator(email, password)) {
            if (auth == null) {
                mListener.onEmailRegisterFail("An Error Occurred, please report to the developer.")
            }

            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        val user: FirebaseUser? = task.result.user
                        mListener.onEmailRegisterSuccess()
                        Log.e("EmailRegisterHelper", "login perform onEmailRegisterSuccess")
                    }
                    else if (task.isCanceled){
                        mListener.onEmailRegisterFail("Registration cancelled")
                    } else {
                        Log.e("EmailRegisterHelper", "login perform failed")
                        Log.e("EmailRegisterHelper", "login perform failed")
                        mListener.onEmailRegisterFail("Authentication failed")
                    }
                }
        } else {
            mListener.onEmailRegisterFail("email or password is invalid.")
            Log.e("EmailSignInHelper", "login email or password is invalid")
        }
    }

    private fun loginValidator(email: String, password: String): Boolean {
        return !(email.isEmpty() || password.isEmpty())
    }

    companion object {
        private const val TAG = "EmailRegisterHelper"
    }
}
