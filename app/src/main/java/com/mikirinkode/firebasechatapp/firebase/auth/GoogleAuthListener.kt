package com.mikirinkode.firebasechatapp.firebase.auth

interface GoogleAuthListener {
    fun onGoogleAuthSignIn(authToken: String?, userId: String?)

    fun onGoogleAuthSignInFailed(errorMessage: String?)

}