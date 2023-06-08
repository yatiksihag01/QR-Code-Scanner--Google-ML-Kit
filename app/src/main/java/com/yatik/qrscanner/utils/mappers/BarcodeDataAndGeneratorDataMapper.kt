package com.yatik.qrscanner.utils.mappers

import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.GeneratorData

fun barcodeDataToGeneratorData(barcodeData: BarcodeData): GeneratorData =
    GeneratorData(
        type = barcodeData.type,
        text = barcodeData.title,
        url = barcodeData.decryptedText,
        ssid = barcodeData.title,
        securityType = barcodeData.others,
        password = barcodeData.decryptedText,
        phone = barcodeData.title,
        message = barcodeData.decryptedText,
        barcodeNumber = barcodeData.title
    )