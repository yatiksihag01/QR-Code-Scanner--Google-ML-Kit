package com.yatik.qrscanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yatik.qrscanner.models.UrlPreviewData

@Dao
interface UrlPreviewDao {

    @Upsert
    suspend fun upsertUrlInfo(urlPreviewData: UrlPreviewData)

    @Delete
    suspend fun deleteUrlInfo(urlPreviewData: UrlPreviewData)

    @Query("SELECT * FROM url_info_table WHERE main_url = :mainUrl")
    suspend fun getUrlInfo(mainUrl: String): UrlPreviewData?

}