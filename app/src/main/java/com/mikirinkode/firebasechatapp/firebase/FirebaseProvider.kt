package com.mikirinkode.firebasechatapp.firebase

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.mikirinkode.firebasechatapp.commonhelper.DateHelper

class FirebaseProvider {
    private var firebaseAuth: FirebaseAuth? = null
    private var firebaseDatabase: FirebaseDatabase? = null
    private var firebaseFirestore: FirebaseFirestore? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var firebaseMessaging: FirebaseMessaging? = null

    fun initialize(context: Context?) {
        if (context != null) {
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseDatabase?.setPersistenceEnabled(true)

            firebaseFirestore = FirebaseFirestore.getInstance()
            firebaseStorage = FirebaseStorage.getInstance()
            firebaseMessaging = FirebaseMessaging.getInstance()
            observeToken()
        }
    }

    private fun observeToken(){
        val currentUserId = firebaseAuth?.currentUser?.uid
        val userRef = currentUserId?.let { firebaseFirestore?.collection("users")?.document(it) }

        firebaseMessaging?.token?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                val token = task.result

                // Save token to server if use a server
                if (currentUserId != null){
                    val currentDate = DateHelper.getCurrentDateTime()
                    val updates = hashMapOf<String, Any>(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to currentDate
                    )
                    userRef?.set(updates, SetOptions.merge())
                }
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

        private var sHelper: FirebaseProvider? = null

        fun instance(): FirebaseProvider {
            if (sHelper == null) {
                sHelper = FirebaseProvider()
            }
            return sHelper as FirebaseProvider
        }
    }
}