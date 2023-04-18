package com.mikirinkode.firebasechatapp.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelper {
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private var firebaseFirestore: FirebaseFirestore? = null

    fun initialize(context: Context?) {
        if (context != null) {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
        }
    }

    fun getFirebaseAuth(): FirebaseAuth? {
        return firebaseAuth
    }

    fun getDatabase(): FirebaseDatabase? {
        return firebaseDatabase
    }

    fun getFirestore(): FirebaseFirestore? {
        return firebaseFirestore
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