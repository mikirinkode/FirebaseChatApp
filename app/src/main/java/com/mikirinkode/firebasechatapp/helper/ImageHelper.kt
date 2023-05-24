package com.mikirinkode.firebasechatapp.helper

import android.content.ContentResolver
import android.media.Image
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

        return "messages/images-" + System.currentTimeMillis() + "." + imageExtension
    }
}