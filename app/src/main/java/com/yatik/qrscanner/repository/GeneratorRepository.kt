package com.yatik.qrscanner.repository

import android.graphics.Bitmap

interface GeneratorRepository {
    suspend fun saveImageToGallery(bitmap: Bitmap): Boolean
}