package com.mikirinkode.firebasechatapp.commonhelper

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap

object ImageHelper {
    fun getPathForMessages(contentResolver: ContentResolver, capturedImage: Uri): String {
        val imageExtension = MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(
                contentResolver.getType(
                    capturedImage
                )
            )

        return "images-" + System.currentTimeMillis() + "." + imageExtension
    }
}