package com.mikirinkode.firebasechatapp.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper

class EmailRegisterHelper(
    private val mListener: EmailRegisterListener
) {

    private val auth: FirebaseAuth? = FirebaseProvider.instance().getFirebaseAuth()
    private val pref = LocalSharedPref.instance()
    private val fireStore = FirebaseProvider.instance().getFirestore()

    fun performRegister(
        name: String,
        email: String,
        password: String,
    ) {
        if (loginValidator(email, password)) {
            if (auth == null) {
                mListener.onEmailRegisterFail("An Error Occurred, please report to the developer.")
            }

            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->


                    if (task.isSuccessful) {

                        val loggedUser: FirebaseUser? = task.result.user

                        val avatarUrl = if (loggedUser?.photoUrl == null) "" else loggedUser.photoUrl.toString()

                        val documentRef =
                            fireStore?.collection("users")?.document(loggedUser?.uid ?: "")
                        val date = DateHelper.getCurrentDateTime()
                        val user = hashMapOf( // TODO
                            "userId" to loggedUser?.uid,
                            "email" to loggedUser?.email,
                            "name" to name,
                            "avatarUrl" to avatarUrl,
                            "createdAt" to date,
                            "lastLoginAt" to date,
                            "updatedAt" to date,
                        )

                        documentRef?.set(user)?.addOnSuccessListener {

                            mListener.onEmailRegisterSuccess()

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

                        }?.addOnFailureListener {
                            mListener.onEmailRegisterFail("Registration failed: ${it.message}")
                        }
                    } else if (task.isCanceled) {
                        mListener.onEmailRegisterFail("Registration cancelled")
                    } else {
                        mListener.onEmailRegisterFail("Authentication failed")
                    }
                }
        } else {
            mListener.onEmailRegisterFail("email or password is invalid.")
        }
    }

    private fun loginValidator(email: String, password: String): Boolean {
        return !(email.isEmpty() || password.isEmpty())
    }

    companion object {
        private const val TAG = "EmailRegisterHelper"
    }
}
