package com.mikirinkode.firebasechatapp.firebase.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper
import com.mikirinkode.firebasechatapp.helper.DateHelper

class EmailRegisterHelper(
    private val mListener: EmailRegisterListener
) {

    private val auth: FirebaseAuth? = FirebaseHelper.instance().getFirebaseAuth()
    private val pref = LocalSharedPref.instance()
    private val fireStore = FirebaseHelper.instance().getFirestore()

    fun performRegister(
        name: String,
        email: String,
        password: String,
    ) {
        Log.e("EmailRegisterHelper", "login perform register")
        if (loginValidator(email, password)) {
            if (auth == null) {
                mListener.onEmailRegisterFail("An Error Occurred, please report to the developer.")
            }
            Log.e("EmailRegisterHelper", "login valid")

            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->

                    Log.e(
                        "EmailRegisterHelper",
                        "login createUserWithEmailAndPassword addOnCompleteListener"
                    )
                    if (task.isSuccessful) {
                        Log.e("EmailRegisterHelper", "task success")
                        val loggedUser: FirebaseUser? = task.result.user

                        val avatarUrl = if (loggedUser?.photoUrl == null) "" else loggedUser.photoUrl.toString()

                        val documentRef =
                            fireStore?.collection("users")?.document(loggedUser?.uid ?: "")
                        val date = DateHelper.getCurrentDateTime()
                        val user = hashMapOf(
                            "userId" to loggedUser?.uid,
                            "email" to loggedUser?.email,
                            "name" to name,
                            "avatarUrl" to avatarUrl,
                            "createdAt" to date,
                            "lastLoginAt" to date,
                            "updatedAt" to date,
                        )

                        documentRef?.set(user)?.addOnSuccessListener {

                            Log.e("EmailRegisterHelper", "doc ref")
                            mListener.onEmailRegisterSuccess()
                            Log.e("EmailRegisterHelper", "login perform onEmailRegisterSuccess")

                            val username = loggedUser?.displayName ?: loggedUser?.email ?: "user"
                            pref?.startSession(
                                UserAccount(
                                    loggedUser?.uid,
                                    loggedUser?.email,
                                    name,
                                    avatarUrl,
                                    date,
                                    date,
                                    date
                                )
                            )
                            Log.e("EmailRegisterHelper", "firestore success")

                        }?.addOnFailureListener {
                            Log.e("EmailRegisterHelper", "firestore failed")
                            mListener.onEmailRegisterFail("Registration failed: ${it.message}")
                        }
                    } else if (task.isCanceled) {
                        mListener.onEmailRegisterFail("Registration cancelled")
                        Log.e("EmailRegisterHelper", "cancelled")
                        Log.e("EmailRegisterHelper", "${task.isSuccessful}")
                    } else {
                        Log.e("EmailRegisterHelper", "${task.result}")
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
