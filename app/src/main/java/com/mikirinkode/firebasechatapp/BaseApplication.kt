package com.mikirinkode.firebasechatapp

import android.app.Application
import com.mikirinkode.firebasechatapp.data.local.pref.LocalSharedPref
import com.mikirinkode.firebasechatapp.firebase.FirebaseHelper

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseHelper.instance().initialize(this)


        LocalSharedPref.init(applicationContext)
    }
}