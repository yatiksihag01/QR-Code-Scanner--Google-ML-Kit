package com.yatik.qrscanner.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class GeneratorData(
    var type: Int,
    var text: String? = null,
    var url: String? = null,
    var ssid: String? = null,
    var securityType: String? = null,
    var password: String? = null,
    var phone: String? = null,
    var message: String? = null,
    var barcodeNumber: String? = null
) : Parcelable
