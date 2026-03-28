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
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "url_info_table")
data class UrlPreviewData(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("main_url")
    @ColumnInfo(name = "main_url") val mainUrl: String,

    @SerializedName("title")
    @ColumnInfo(name = "title") val title: String?,

    @SerializedName("description")
    @ColumnInfo(name = "description") val description: String?,
    @SerializedName("image_url")
    @ColumnInfo(name = "image_url") val imageUrl: String?
) : Parcelable
