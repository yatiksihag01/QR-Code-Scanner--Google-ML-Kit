package com.yatik.qrscanner.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

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
