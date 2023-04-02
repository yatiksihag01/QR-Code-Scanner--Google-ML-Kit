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
import com.google.zxing.qrcode.QRCodeWriter
import com.yatik.qrscanner.models.GeneratorData
import com.yatik.qrscanner.repository.GeneratorRepository
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
                Barcode.TYPE_TEXT -> {
                    bitMatrix = QRCodeWriter().encode(generatorData.text, BarcodeFormat.QR_CODE, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT)
                }
                Barcode.TYPE_WIFI -> {
                    val wifiString = "WIFI:S:${generatorData.ssid};T:${generatorData.securityType};P:${generatorData.password};"
                    bitMatrix = MultiFormatWriter().encode(wifiString, BarcodeFormat.QR_CODE, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT)
                }
                Barcode.TYPE_URL -> {
                    bitMatrix = MultiFormatWriter().encode(generatorData.url, BarcodeFormat.QR_CODE, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT)
                }
                Barcode.TYPE_SMS -> {
                    bitMatrix = MultiFormatWriter().encode(
                        "smsto:${generatorData.phone}:${generatorData.message}",
                        BarcodeFormat.QR_CODE,
                        QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT
                    )
                }
                Barcode.TYPE_PHONE -> {
                bitMatrix = MultiFormatWriter().encode("tel:${generatorData.phone}", BarcodeFormat.QR_CODE, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT)
                }
                else -> bitMatrix = EAN13Writer().encode(generatorData.barcodeNumber, BarcodeFormat.EAN_13, QR_WIDTH_HEIGHT, QR_WIDTH_HEIGHT)
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
        _bitmap.postValue(
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        )
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

}