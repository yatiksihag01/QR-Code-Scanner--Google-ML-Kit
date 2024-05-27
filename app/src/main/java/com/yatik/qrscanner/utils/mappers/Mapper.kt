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

package com.yatik.qrscanner.utils.mappers

import android.os.Build
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.data.CalendarEvent
import com.yatik.qrscanner.models.barcode.data.Contact
import com.yatik.qrscanner.models.barcode.data.ContactType
import com.yatik.qrscanner.models.barcode.data.Email
import com.yatik.qrscanner.models.barcode.data.Geo
import com.yatik.qrscanner.models.barcode.data.Phone
import com.yatik.qrscanner.models.barcode.data.Security
import com.yatik.qrscanner.models.barcode.data.Sms
import com.yatik.qrscanner.models.barcode.data.Url
import com.yatik.qrscanner.models.barcode.data.WiFi
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Mapper {
    fun fromBarcodeToBarcodeDetails(barcode: Barcode): BarcodeDetails {
        return if (barcode.format == Barcode.FORMAT_QR_CODE) {
            mapForQRCodes(barcode)!!
        } else {
            BarcodeDetails(
                format(barcode.format),
                type(barcode.valueType),
                dateTime(),
                "${barcode.rawValue}"
            )
        }
    }

    fun fromBarcodeDataToBarcodeDetails(barcodeData: BarcodeData): BarcodeDetails {
        return if (barcodeData.format == Barcode.FORMAT_QR_CODE) {
            mapForQRCodes(barcodeData)!!
        } else {
            BarcodeDetails(
                format(barcodeData.format),
                type(barcodeData.type),
                barcodeData.dateTime,
                "${barcodeData.title}"
            )
        }
    }

    fun fromBarcodeDetailsToJson(barcodeDetails: BarcodeDetails): String {
        return Gson().toJson(barcodeDetails)
    }

    private fun mapForQRCodes(barcode: Barcode): BarcodeDetails? {
        val format = format(barcode.format)
        val type = type(barcode.valueType)
        val rawValue = "${barcode.rawValue}"
        val dateTime = dateTime()

        if (format != Format.QR_CODE) return null

        when (barcode.valueType) {
            Barcode.TYPE_TEXT -> {
                return BarcodeDetails(format, type, dateTime, rawValue, text = barcode.displayValue)
            }

            Barcode.TYPE_URL -> {
                val url = Url(barcode.url!!.title, barcode.url!!.url)
                return BarcodeDetails(format, type, dateTime, rawValue, url = url)
            }

            Barcode.TYPE_EMAIL -> {
                val email = Email(
                    barcode.email?.address,
                    barcode.email?.subject,
                    barcode.email?.body,
                    contactType(barcode.email?.type)
                )
                return BarcodeDetails(format, type, dateTime, rawValue, email = email)
            }

            Barcode.TYPE_WIFI -> {
                val wifi = WiFi(
                    barcode.wifi?.ssid,
                    barcode.wifi?.password,
                    securityType(barcode.wifi?.encryptionType)
                )
                return BarcodeDetails(format, type, dateTime, rawValue, wiFi = wifi)
            }

            Barcode.TYPE_CONTACT_INFO -> {
                val contact = Contact(
                    barcode.contactInfo?.name?.formattedName,
                    barcode.contactInfo?.phones?.get(0)?.number,
                    null,
                    barcode.contactInfo?.emails?.get(0)?.address,
                    null
                )
                return BarcodeDetails(format, type, dateTime, rawValue, contact = contact)
            }

            Barcode.TYPE_PHONE -> {
                val phone = Phone(null, barcode.phone?.number, contactType(barcode.phone?.type))
                return BarcodeDetails(format, type, dateTime, rawValue, phone = phone)
            }

            Barcode.TYPE_SMS -> {
                val sms = Sms(barcode.sms?.phoneNumber, barcode.sms?.message)
                return BarcodeDetails(format, type, dateTime, rawValue, sms = sms)
            }

            Barcode.TYPE_CALENDAR_EVENT -> {
                val event = barcode.calendarEvent!!
                val calendarEvent = CalendarEvent(
                    event.description,
                    event.start.toString(),
                    event.end.toString(),
                    event.organizer
                )
                return BarcodeDetails(
                    format,
                    type,
                    dateTime,
                    rawValue,
                    calendarEvent = calendarEvent
                )
            }

            Barcode.TYPE_GEO -> {
                val location = Geo(
                    barcode.geoPoint?.lat,
                    barcode.geoPoint?.lng
                )
                return BarcodeDetails(format, type, dateTime, rawValue, geo = location)
            }

            Barcode.TYPE_ISBN -> {
                return BarcodeDetails(format, type, dateTime, rawValue, isbn = rawValue)
            }

            else -> return BarcodeDetails(format, type, dateTime, rawValue)
        }
    }

    private fun mapForQRCodes(barcodeData: BarcodeData): BarcodeDetails? {
        val format = format(barcodeData.format)
        val type = type(barcodeData.type)
        val rawValue = "${barcodeData.title}"

        if (format != Format.QR_CODE) return null

        when (barcodeData.type) {
            Barcode.TYPE_TEXT -> {
                return BarcodeDetails(format, type, barcodeData.dateTime, rawValue, text = rawValue)
            }

            Barcode.TYPE_URL -> {
                val url = Url(barcodeData.title, barcodeData.decryptedText)
                return BarcodeDetails(format, type, barcodeData.dateTime, rawValue, url = url)
            }

            Barcode.TYPE_WIFI -> {
                val wifi = WiFi(
                    barcodeData.title,
                    barcodeData.decryptedText,
                    securityType(barcodeData.others)
                )
                return BarcodeDetails(format, type, barcodeData.dateTime, rawValue, wiFi = wifi)
            }

            Barcode.TYPE_PHONE -> {
                val phone = Phone(null, barcodeData.title, null)
                return BarcodeDetails(format, type, barcodeData.dateTime, rawValue, phone = phone)
            }

            Barcode.TYPE_SMS -> {
                val sms = Sms(barcodeData.title, barcodeData.decryptedText)
                return BarcodeDetails(format, type, barcodeData.dateTime, rawValue, sms = sms)
            }

            Barcode.TYPE_GEO -> {
                val longLatList = barcodeData.others?.split(",")
                val location = Geo(longLatList?.get(0)?.toDouble(), longLatList?.get(1)?.toDouble())
                return BarcodeDetails(format, type, barcodeData.dateTime, rawValue, geo = location)
            }

            else -> return BarcodeDetails(format, type, barcodeData.dateTime, rawValue)
        }
    }

    private fun contactType(type: Int?): ContactType {
        return when (type) {
            Barcode.Email.TYPE_HOME -> ContactType.HOME
            Barcode.Email.TYPE_WORK -> ContactType.WORK
            else -> ContactType.OTHER
        }
    }

    private fun securityType(type: Int?): Security {
        return when (type) {
            Barcode.WiFi.TYPE_WEP -> Security.WEP
            Barcode.WiFi.TYPE_WPA -> Security.WPA
            else -> Security.OPEN
        }
    }

    private fun securityType(type: String?): Security {
        return when (type) {
            "WEP" -> Security.WEP
            "WPA" -> Security.WPA
            else -> Security.OPEN
        }
    }

    private fun format(format: Int): Format {
        return when (format) {
            Barcode.FORMAT_AZTEC -> Format.AZTEC
            Barcode.FORMAT_CODABAR -> Format.CODABAR
            Barcode.FORMAT_CODE_39 -> Format.CODE_39
            Barcode.FORMAT_CODE_93 -> Format.CODE_93
            Barcode.FORMAT_CODE_128 -> Format.CODE_128
            Barcode.FORMAT_DATA_MATRIX -> Format.DATA_MATRIX
            Barcode.FORMAT_EAN_8 -> Format.EAN_8
            Barcode.FORMAT_EAN_13 -> Format.EAN_13
            Barcode.FORMAT_ITF -> Format.ITF
            Barcode.FORMAT_PDF417 -> Format.PDF417
            Barcode.FORMAT_QR_CODE -> Format.QR_CODE
            Barcode.FORMAT_UPC_A -> Format.UPC_A
            Barcode.FORMAT_UPC_E -> Format.UPC_E
            else -> Format.UNKNOWN
        }
    }

    private fun type(type: Int): Type {
        return when (type) {
            Barcode.TYPE_CONTACT_INFO -> Type.TYPE_CONTACT
            Barcode.TYPE_EMAIL -> Type.TYPE_EMAIL
            Barcode.TYPE_PHONE -> Type.TYPE_PHONE
            Barcode.TYPE_PRODUCT -> Type.TYPE_PRODUCT
            Barcode.TYPE_SMS -> Type.TYPE_SMS
            Barcode.TYPE_TEXT -> Type.TYPE_TEXT
            Barcode.TYPE_URL -> Type.TYPE_URL
            Barcode.TYPE_WIFI -> Type.TYPE_WIFI
            Barcode.TYPE_GEO -> Type.TYPE_GEO
            Barcode.TYPE_CALENDAR_EVENT -> Type.TYPE_CALENDAR
            Barcode.TYPE_DRIVER_LICENSE -> Type.TYPE_DRIVER_LICENSE
            Barcode.TYPE_ISBN -> Type.TYPE_ISBN
            else -> Type.TYPE_UNKNOWN
        }
    }

    private fun dateTime(): String {
        val dateTime: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            formatter.format(date)
        }
        return dateTime
    }

}
