package com.mikirinkode.firebasechatapp.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.firebasechatapp.R

class FirebaseHelper {
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private var firebaseFirestore: FirebaseFirestore? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var firebaseMessaging: FirebaseMessaging? = null

    fun initialize(context: Context?) {
        if (context != null) {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseFirestore = FirebaseFirestore.getInstance()
            firebaseStorage = FirebaseStorage.getInstance()
            firebaseMessaging = FirebaseMessaging.getInstance()
            observeToken()
        }
    }

    fun observeToken(){
        firebaseMessaging?.token?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                val token = task.result
                Log.d("FirebaseHelper", "Token: $token")

                // Todo: Save token to server if use a server
            }
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

    fun getStorage(): FirebaseStorage? {
        return firebaseStorage
    }

    fun getMessaging(): FirebaseMessaging? {
        return firebaseMessaging
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