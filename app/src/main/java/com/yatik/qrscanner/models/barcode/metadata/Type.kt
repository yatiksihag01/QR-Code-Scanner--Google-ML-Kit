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

package com.yatik.qrscanner.models.barcode.metadata

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Enum class representing different types of QR codes.
 */
@Keep
enum class Type {
    @SerializedName("TYPE_UNKNOWN") TYPE_UNKNOWN,
    @SerializedName("TYPE_TEXT") TYPE_TEXT,
    @SerializedName("TYPE_URL") TYPE_URL,
    @SerializedName("TYPE_WIFI") TYPE_WIFI,
    @SerializedName("TYPE_EMAIL") TYPE_EMAIL,
    @SerializedName("TYPE_PHONE") TYPE_PHONE,
    @SerializedName("TYPE_ISBN") TYPE_ISBN,
    @SerializedName("TYPE_GEO") TYPE_GEO,
    @SerializedName("TYPE_CONTACT") TYPE_CONTACT,
    @SerializedName("TYPE_CALENDAR") TYPE_CALENDAR,
    @SerializedName("TYPE_PRODUCT") TYPE_PRODUCT,
    @SerializedName("TYPE_SMS") TYPE_SMS,
    @SerializedName("TYPE_DRIVER_LICENSE") TYPE_DRIVER_LICENSE
}