package com.mikirinkode.firebasechatapp

import android.app.Application
import android.content.Context
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.firebase.FirebaseProvider

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        mContext = this

        FirebaseProvider.instance().initialize(this)
        LocalSharedPref.init(applicationContext)
    }

    companion object {
        lateinit var mContext: Context
    }
}