package com.flash.recipeVault.util

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.net.toUri
//import com.flash.recipeVault.util.ImageBase64Util.base64ToImageBitmap

object ImageBase64Util {

    /**
     * Reads the given content URI and returns Base64 (without data: prefix).
     * Returns null if uri is null or can't be read.
     */
//    fun tryReadAsBase64(context: Context, uriString: String?): String? {
//        if (uriString.isNullOrBlank()) return null
//        return try {
//            val uri = uriString.toUri()
//            context.contentResolver.openInputStream(uri)?.use { input ->
//                val bytes = input.readBytes()
//                Base64.encodeToString(bytes, Base64.NO_WRAP)
//            }
//        } catch (_: Exception) {
//            null
//        }
//    }
//
//    fun base64ToImageBitmap(base64: String): ImageBitmap? {
//        return try {
//            val cleanBase64 = base64.substringAfter(",") // removes data:image/... if present
//            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
//            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
//                ?.asImageBitmap()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }
}

//@Composable
//fun rememberBase64ImageBitmap(base64: String?): ImageBitmap? {
//    return remember(base64) {
//        base64?.let { base64ToImageBitmap(it) }
//    }
//}
