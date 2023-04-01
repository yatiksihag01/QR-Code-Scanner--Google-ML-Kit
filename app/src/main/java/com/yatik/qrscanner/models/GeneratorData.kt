package com.yatik.qrscanner.models

data class GeneratorData(
    val type: Int,
    val text: String?,
    val url: String?,
    val ssid: String?,
    val securityType: String?,
    val password: String?,
    val phone: String?,
    val message: String?,
    val barcodeNumber: String?
)
