/*
 * Copyright 2024 Yatik
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

package com.yatik.qrscanner.models.barcode

import android.os.Parcelable
import androidx.annotation.Keep
import com.yatik.qrscanner.models.barcode.data.CalendarEvent
import com.yatik.qrscanner.models.barcode.data.Contact
import com.yatik.qrscanner.models.barcode.data.Email
import com.yatik.qrscanner.models.barcode.data.Geo
import com.yatik.qrscanner.models.barcode.data.Phone
import com.yatik.qrscanner.models.barcode.data.Sms
import com.yatik.qrscanner.models.barcode.data.Url
import com.yatik.qrscanner.models.barcode.data.WiFi
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import kotlinx.parcelize.Parcelize

/**
 * Stores the details of a scanned barcode / QR Code.
 */
@Keep
@Parcelize
data class BarcodeDetails (
    /**
     * The barcode or QR code format.
     * */
    val format: Format,

    /**
     * The type of QR Code, if applicable.
     */
    val type: Type,

    /**
     * The date and time of scanning in DD-MM-YYYY HH:MM:SS format.
     */
    val timeStamp: String?,

    /**
     * The raw and unmodified content of the barcode or QR Code.
     */
    val rawValue: String,

    /**
     * The text content of QR Code.
     */
    val text: String? = null,

    /**
     * The [Sms] extracted from the barcode or QR code, if applicable.
     */
    val sms: Sms? = null,

    /**
     * The [Url] extracted from the barcode or QR code, if applicable.
     */
    val url: Url? = null,

    /**
     * The [WiFi] information such as SSID, password,
     * and [com.yatik.qrscanner.models.barcode.data.Security] type extracted from QR code, if applicable.
     */
    val wiFi: WiFi? = null,

    /**
     * The [Phone] contact extracted from the barcode or QR code, if applicable.
     */
    val phone: Phone? = null,

    /**
     * The [Email] extracted from the QR code, if applicable.
     */
    val email: Email? = null,

    /**
     * The [Geo] coordinates extracted from the QR code, if applicable.
     */
    val geo: Geo? = null,

    /**
     * The [Contact] extracted from the QR code, if applicable.
     */
    val contact: Contact? = null,

    /**
     * The [CalendarEvent] extracted from the QR code, if applicable.
     */
    val calendarEvent: CalendarEvent? = null,

    /**
     * The ISBN number extracted from the QR code, if applicable.
     */
    val isbn: String? = null
) : Parcelable