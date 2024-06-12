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
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.EAN13Writer
import com.google.zxing.oned.EAN8Writer
import com.google.zxing.oned.UPCAWriter
import com.google.zxing.oned.UPCEWriter
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
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

    fun generateBarcode(barcodeDetails: BarcodeDetails) = viewModelScope.launch {
        val bitMatrix: BitMatrix
        try {
            when (barcodeDetails.format) {
                Format.QR_CODE -> bitMatrix = generateQRCode(barcodeDetails)
                Format.EAN_13 -> bitMatrix = EAN13Writer().encode(
                    barcodeDetails.rawValue,
                    BarcodeFormat.EAN_13,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                Format.EAN_8 -> bitMatrix = EAN8Writer().encode(
                    barcodeDetails.rawValue,
                    BarcodeFormat.EAN_8,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                Format.UPC_A -> bitMatrix = UPCAWriter().encode(
                    barcodeDetails.rawValue,
                    BarcodeFormat.UPC_A,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                Format.UPC_E -> bitMatrix = UPCEWriter().encode(
                    barcodeDetails.rawValue,
                    BarcodeFormat.UPC_E,
                    QR_WIDTH_HEIGHT,
                    QR_WIDTH_HEIGHT
                )

                else -> bitMatrix = multiFormatWriter(
                    barcodeDetails.rawValue,
                    getBarcodeFormat(barcodeDetails.format)
                )
            }
            renderIntoBitmap(bitMatrix)
            _isQRGeneratedSuccessfully.postValue(true)
        } catch (writerException: WriterException) {
            writerException.printStackTrace()
            _isQRGeneratedSuccessfully.postValue(false)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            _isQRGeneratedSuccessfully.postValue(false)
        }
    }

    /**
     * @throws IllegalArgumentException if the format is not [Format.QR_CODE].
     * @throws WriterException if an error occurs while generating the QR code.
     */
    @Throws(WriterException::class, IllegalArgumentException::class)
    private fun generateQRCode(barcodeDetails: BarcodeDetails): BitMatrix {
        if (barcodeDetails.format != Format.QR_CODE) {
            throw IllegalArgumentException("Invalid barcode format")
        }
        return when (barcodeDetails.type) {
            Type.TYPE_TEXT -> multiFormatWriter(barcodeDetails.text, BarcodeFormat.QR_CODE)
            Type.TYPE_WIFI -> multiFormatWriter(
                "WIFI:S:${barcodeDetails.wiFi?.ssid};" +
                        "T:${barcodeDetails.wiFi?.security};" +
                        "P:${barcodeDetails.wiFi?.password};",
                BarcodeFormat.QR_CODE
            )

            Type.TYPE_URL -> multiFormatWriter(barcodeDetails.url?.url, BarcodeFormat.QR_CODE)
            Type.TYPE_SMS -> multiFormatWriter(
                "smsto:${barcodeDetails.sms?.number}:${barcodeDetails.sms?.message}",
                BarcodeFormat.QR_CODE
            )

            Type.TYPE_PHONE -> multiFormatWriter(
                "tel:${barcodeDetails.phone?.number}",
                BarcodeFormat.QR_CODE
            )

            else -> multiFormatWriter(
                barcodeDetails.rawValue,
                getBarcodeFormat(barcodeDetails.format)
            )
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

    @Throws(WriterException::class)
    private fun multiFormatWriter(content: String?, format: BarcodeFormat): BitMatrix =
        MultiFormatWriter().encode(content, format, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT)

    /**
     * @throws IllegalArgumentException if the format is not supported.
     */
    @Throws(IllegalArgumentException::class)
    private fun getBarcodeFormat(format: Format): BarcodeFormat {
        return when (format) {
            Format.QR_CODE -> BarcodeFormat.QR_CODE
            Format.EAN_13 -> BarcodeFormat.EAN_13
            Format.EAN_8 -> BarcodeFormat.EAN_8
            Format.UPC_A -> BarcodeFormat.UPC_A
            Format.UPC_E -> BarcodeFormat.UPC_E
            Format.CODE_128 -> BarcodeFormat.CODE_128
            Format.CODE_39 -> BarcodeFormat.CODE_39
            Format.CODE_93 -> BarcodeFormat.CODE_93
            Format.AZTEC -> BarcodeFormat.AZTEC
            Format.CODABAR -> BarcodeFormat.CODABAR
            Format.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            else -> throw IllegalArgumentException("Invalid barcode format")
        }
    }

}