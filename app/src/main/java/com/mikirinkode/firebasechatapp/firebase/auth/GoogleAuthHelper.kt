package com.mikirinkode.firebasechatapp.firebase.auth

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.data.model.UserAccount
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper
import com.mikirinkode.firebasechatapp.helper.DateHelper
import com.mikirinkode.firebasechatapp.utils.Constants

class GoogleAuthHelper(
    private val mActivity: Activity?,
    private val mListener: GoogleAuthListener?,
) {

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var pref: LocalSharedPref? = null
    private val fireStore = FirebaseHelper.instance().getFirestore()

    init {
        if (mListener == null) {
            throw RuntimeException("GoogleAuthResponse listener cannot be null.")
        }
        buildSignInOptions()
        mAuth = FirebaseHelper.instance().getFirebaseAuth()
        pref = LocalSharedPref.instance()
    }

    private fun buildSignInOptions() {
        val gso: GoogleSignInOptions =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.SERVER_ID_TOKEN)
                .requestEmail()
                .build()

        if (mActivity != null) {
            mGoogleSignInClient = GoogleSignIn.getClient(mActivity, gso)
        }
    }


    fun performSignIn() {
        val signInIntent = mGoogleSignInClient?.signInIntent
        mActivity?.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?,
    ) {

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(
        completedTask: Task<GoogleSignInAccount>
    ) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                mAuth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val googleAccount: GoogleAuthUser = parseToGoogleUser(account)
                        val loggedUser = mAuth?.currentUser

                        val documentRef =
                            loggedUser?.uid?.let { fireStore?.collection("users")?.document(it) }
                        val date = DateHelper.getCurrentDate()
                        val user = hashMapOf(
                            "userId" to loggedUser?.uid,
                            "email" to googleAccount.email,
                            "name" to googleAccount.name,
                            "avatarUrl" to googleAccount.photoUrl,
                            "createdAt" to date,
                            "lastLoginAt" to date,
                            "updatedAt" to date,
                        )
                        documentRef?.set(user)?.addOnSuccessListener {

                            pref?.startSession(
                                UserAccount(
                                    loggedUser.uid,
                                    googleAccount.email,
                                    googleAccount.name,
                                    googleAccount.photoUrl.toString(),
                                    date,
                                    date,
                                    date
                                )
                            )

                            mListener?.onGoogleAuthSignIn(
                                googleAccount.idToken,
                                googleAccount.id
                            )
                        }?.addOnFailureListener {
                            Log.e("googleAuthHelper", "firestore failed")
                            mListener?.onGoogleAuthSignInFailed("Registration failed: ${it.message}")
                        }
                    }
                }
            }
        } catch (e: ApiException) {
            mListener?.onGoogleAuthSignInFailed(e.message)
            Log.e(TAG, e.message.toString())
        }
    }

    private fun parseToGoogleUser(account: GoogleSignInAccount): GoogleAuthUser {
        val user = GoogleAuthUser()
        user.name = account.displayName
        user.familyName = account.familyName
        user.idToken = account.idToken
        user.email = account.email
        user.photoUrl = account.photoUrl
        return user
    }

    fun performSignOut() {
        if (mAuth != null && mActivity != null) {
            mAuth?.signOut()
            mGoogleSignInClient?.signOut()?.addOnCompleteListener(mActivity) { }
        }
    }

    companion object {
        private const val TAG = "GoogleSignInHelper"
        private const val RC_SIGN_IN = 9001
    }
}