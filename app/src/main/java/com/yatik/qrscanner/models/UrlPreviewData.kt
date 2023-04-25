package com.yatik.qrscanner.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "url_info_table")
data class UrlPreviewData(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "main_url") val mainUrl: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?
) : Parcelable