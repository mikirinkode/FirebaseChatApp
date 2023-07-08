package com.mikirinkode.firebasechatapp.firebase.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.toObject
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class EmailLoginHelper(
    private val mListener: EmailLoginListener,
) {

    private val auth: FirebaseAuth? = FirebaseProvider.instance().getFirebaseAuth()
    private val pref = LocalSharedPref.instance()
    private val fireStore = FirebaseProvider.instance().getFirestore()

    fun performLogin(
        email: String,
        password: String
    ) {
        if (loginValidator(email, password)) {
            if (auth == null) {
                mListener.onEmailLoginFail("An Error Occurred, please report to the developer.")
            }
            auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // TODO: update last login
                    val userRef = fireStore?.collection("users")?.document(user?.uid ?: "")
                    userRef?.get()
                        ?.addOnSuccessListener { document ->
                            val userAccount: UserAccount? = document.toObject()

                            if (userAccount != null) {
                                mListener.onEmailLoginSuccess(user?.uid)
                                pref?.startSession(userAccount)
                            } else {
                                mListener.onEmailLoginFail("Authentication failed to get User Data")
                            }

                        }?.addOnFailureListener {
                            mListener.onEmailLoginFail("Authentication failed to get User Data")
                        }
                } else {
                    mListener.onEmailLoginFail("Authentication failed")
                }
            }
        } else {
            mListener.onEmailLoginFail("email or password is invalid.")
        }
    }

    private fun loginValidator(email: String, password: String): Boolean {
        return !(email.isEmpty() || password.isEmpty())
    }

    companion object {
        private const val TAG = "EmailSignInHelper"
    }
}