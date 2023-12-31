package com.mikirinkode.firebasechatapp.commonhelper

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.mikirinkode.firebasechatapp.feature.chat.chatroom.ConversationActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(
    private val mListener: CameraListener,
    private val mActivity: Activity,
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

                if (PermissionHelper.isCameraPermissionGranted(mActivity)) {
                    // Launch the camera intent
                    mActivity.startActivityForResult(takePictureIntent, ConversationActivity.REQUEST_IMAGE_CAPTURE)
                    if (capturedImage != null){
                        mListener.onImageCaptured(capturedImage)
                    }
                } else {
                    PermissionHelper.requestCameraPermission(mActivity)
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