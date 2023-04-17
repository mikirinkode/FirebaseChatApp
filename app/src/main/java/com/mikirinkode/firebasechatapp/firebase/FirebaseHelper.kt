package com.mikirinkode.firebasechatapp.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class FirebaseHelper {
    private var firebaseAuth: FirebaseAuth? = null

    fun initialize(context: Context?) {
        if (context != null) {
            firebaseAuth = FirebaseAuth.getInstance()
        }
    }

    fun getFirebaseAuth(): FirebaseAuth? {
        return firebaseAuth
    }

    companion object {

        private var sHelper: FirebaseHelper? = null

        fun instance(): FirebaseHelper {
            if (sHelper == null) {
                sHelper = FirebaseHelper()
            }
            return sHelper as FirebaseHelper
        }
    }
}