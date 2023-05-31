package com.mikirinkode.firebasechatapp.helper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.mikirinkode.firebasechatapp.feature.chat.ChatActivity
import com.mikirinkode.firebasechatapp.utils.PermissionManager
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(
    private val mActivity: Activity,
    private val mListener: CameraListener
) {
    private var capturedImage: Uri? = null

    fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(mActivity.packageManager) != null) {
            // Create a file to save the image
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Handle the error
                null
            }

            // Continue only if the file was successfully created
            photoFile?.let {
                // Get the content URI for the file using FileProvider
                capturedImage = FileProvider.getUriForFile(
                    mActivity,
                    "com.mikirinkode.firebasechatapp.fileprovider",
                    it
                )

                // Set the output file URI for the camera intent
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImage)

                if (PermissionManager.isCameraPermissionGranted(mActivity)) {
                    // Launch the camera intent
                    mActivity.startActivityForResult(takePictureIntent, ChatActivity.REQUEST_IMAGE_CAPTURE)
                    if (capturedImage != null){
                        mListener.onImageCaptured(capturedImage)
                    }
                } else {
                    PermissionManager.requestCameraPermission(mActivity)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
}

interface CameraListener {
    fun onImageCaptured(capturedImage: Uri?)
}