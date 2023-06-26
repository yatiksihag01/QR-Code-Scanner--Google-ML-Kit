package com.yatik.qrscanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yatik.qrscanner.models.UrlPreviewData

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

@Dao
interface UrlPreviewDao {

    @Upsert
    suspend fun upsertUrlInfo(urlPreviewData: UrlPreviewData)

    @Delete
    suspend fun deleteUrlInfo(urlPreviewData: UrlPreviewData)

    @Query("SELECT * FROM url_info_table WHERE main_url = :mainUrl")
    suspend fun getUrlInfo(mainUrl: String): UrlPreviewData?

}