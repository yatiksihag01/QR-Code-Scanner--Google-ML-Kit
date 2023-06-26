package com.yatik.qrscanner.utils.mappers

import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.GeneratorData

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