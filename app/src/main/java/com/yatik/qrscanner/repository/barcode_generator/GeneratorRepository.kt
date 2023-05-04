package com.yatik.qrscanner.repository.barcode_generator

import android.graphics.Bitmap

interface GeneratorRepository {
    suspend fun saveImageToGallery(bitmap: Bitmap): Boolean
}