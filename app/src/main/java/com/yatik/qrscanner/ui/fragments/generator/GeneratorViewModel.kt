/*
 * Copyright 2023 Yatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yatik.qrscanner.ui.fragments.generator

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.EAN13Writer
import com.google.zxing.oned.EAN8Writer
import com.google.zxing.oned.UPCAWriter
import com.google.zxing.oned.UPCEWriter
import com.yatik.qrscanner.models.GeneratorData
import com.yatik.qrscanner.repository.barcode_generator.GeneratorRepository
import com.yatik.qrscanner.utils.Constants.Companion.QR_WIDTH_HEIGHT
import com.yatik.qrscanner.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GeneratorViewModel @Inject constructor(
    private val generatorRepository: GeneratorRepository
) : ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap>()
    val bitmap: LiveData<Bitmap>
        get() = _bitmap

    private var _isQRGeneratedSuccessfully = SingleLiveEvent<Boolean>()
    val isQRGeneratedSuccessfully: SingleLiveEvent<Boolean>
        get() = _isQRGeneratedSuccessfully

    private var _imageSaved = SingleLiveEvent<Boolean>()
    val imageSaved: SingleLiveEvent<Boolean>
        get() = _imageSaved

    fun generateQRCode(generatorData: GeneratorData) = viewModelScope.launch {
        val bitMatrix: BitMatrix
        try {
            when (generatorData.type) {
                Barcode.TYPE_TEXT -> bitMatrix = multiFormatWriter(generatorData.text)
                Barcode.TYPE_WIFI -> bitMatrix = multiFormatWriter(
                    "WIFI:S:${generatorData.ssid};T:${generatorData.securityType};P:${generatorData.password};"
                )

                Barcode.TYPE_URL -> bitMatrix = multiFormatWriter(generatorData.url)
                Barcode.TYPE_SMS -> bitMatrix = multiFormatWriter(
                    "smsto:${generatorData.phone}:${generatorData.message}"
                )

                Barcode.TYPE_PHONE -> bitMatrix = multiFormatWriter(
                    "tel:${generatorData.phone}"
                )

                Barcode.FORMAT_EAN_13 -> bitMatrix = EAN13Writer().encode(
                    generatorData.barcodeNumber,
                    BarcodeFormat.EAN_13,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                Barcode.FORMAT_EAN_8 -> bitMatrix = EAN8Writer().encode(
                    generatorData.barcodeNumber,
                    BarcodeFormat.EAN_8,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                Barcode.FORMAT_UPC_A -> bitMatrix = UPCAWriter().encode(
                    generatorData.barcodeNumber,
                    BarcodeFormat.UPC_A,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                Barcode.FORMAT_UPC_E -> bitMatrix = UPCEWriter().encode(
                    generatorData.barcodeNumber,
                    BarcodeFormat.UPC_E,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                else -> bitMatrix = MultiFormatWriter().encode(
                    generatorData.barcodeNumber,
                    BarcodeFormat.CODE_128,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )
            }
            renderIntoBitmap(bitMatrix)
            _isQRGeneratedSuccessfully.postValue(true)
        } catch (writerException: WriterException) {
            writerException.printStackTrace()
            _isQRGeneratedSuccessfully.postValue(false)
        } catch (e: Exception) {
            e.printStackTrace()
            _isQRGeneratedSuccessfully.postValue(false)
        }
    }

    @Throws(java.lang.IllegalArgumentException::class)
    private fun renderIntoBitmap(bitMatrix: BitMatrix) {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        _bitmap.postValue(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        })
    }

    fun saveImageToGallery() = viewModelScope.launch(Dispatchers.IO) {
        val result = withContext(Dispatchers.IO) {
            bitmap.value?.let {
                generatorRepository.saveImageToGallery(it)
            } ?: false
        }
        withContext(Dispatchers.Main) {
            _imageSaved.value = result
        }
    }

    private fun multiFormatWriter(content: String?): BitMatrix = MultiFormatWriter().encode(
        content, BarcodeFormat.QR_CODE, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT
    )

}