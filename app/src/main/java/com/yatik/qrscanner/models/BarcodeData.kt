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

package com.yatik.qrscanner.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


/**
 * SSID, title, number, phone_number, raw, barcodes => title: String
 *
 * password, url, message => decryptedText: String
 *
 * encryptionType, ($latitude,$longitude) => others: String
 *
 * */

// Barcode.FORMAT_QR_CODE = 256

@Keep
@Parcelize
@Entity(tableName = "barcode_table")
@Deprecated(
    message = "The BarcodeData class is deprecated due to unclear field roles." +
            " Use BarcodeDetails instead, which provides a clearer" +
            " definition of each field's purpose.",
    replaceWith = ReplaceWith("BarcodeDetails",
        "com.yatik.qrscanner.models.barcode.BarcodeDetails"),
    level = DeprecationLevel.WARNING
)
data class BarcodeData(
    @ColumnInfo(name = "format", defaultValue = "256") val format: Int,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "decryptedText") val decryptedText: String?,
    @ColumnInfo(name = "others") val others: String?,
    @ColumnInfo(name = "dateTime") val dateTime: String,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Int = 0
) : Parcelable