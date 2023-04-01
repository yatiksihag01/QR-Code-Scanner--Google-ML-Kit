package com.yatik.qrscanner.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DefaultGeneratorRepository @Inject constructor(
    @ApplicationContext val context: Context
) : GeneratorRepository {

    override suspend fun saveImageToGallery(bitmap: Bitmap): Boolean {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageName = "QRCode_$timeStamp.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        val resolver = context.contentResolver
        return try {
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?.also { resultUri ->
                    resolver.openOutputStream(resultUri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                } ?: throw IOException("Unable to write data")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

}