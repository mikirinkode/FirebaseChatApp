package com.mikirinkode.firebasechatapp.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

object PermissionManager {

    const val CAMERA_REQUEST_PERMISSION_CODE = 9001
    const val READ_EXTERNAL_REQUEST_PERMISSION_CODE = 9002

    fun requestCameraPermission(activity: Activity){
        val requestPermissions = mutableListOf<String>()

        if (!isCameraPermissionGranted(activity)){
            requestPermissions.add(android.Manifest.permission.CAMERA)
        }

        if (requestPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(activity, requestPermissions.toTypedArray(), CAMERA_REQUEST_PERMISSION_CODE)
        }
    }

    fun isCameraPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestReadExternalPermission(activity: Activity){
        val requestPermissions = mutableListOf<String>()

        if (!isCameraPermissionGranted(activity)){
            requestPermissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (requestPermissions.isNotEmpty()){
            ActivityCompat.requestPermissions(activity, requestPermissions.toTypedArray(), READ_EXTERNAL_REQUEST_PERMISSION_CODE)
        }
    }

    fun isReadExternalPermissionGranted(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationPermissionGranted(context: Context): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }
}