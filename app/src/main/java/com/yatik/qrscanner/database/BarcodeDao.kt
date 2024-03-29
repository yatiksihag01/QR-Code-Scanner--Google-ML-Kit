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

package com.yatik.qrscanner.database

import androidx.paging.PagingSource
import androidx.room.*
import com.yatik.qrscanner.models.BarcodeData

@Dao
interface BarcodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(barcodeData: BarcodeData)

    @Delete
    suspend fun delete(barcodeData: BarcodeData)

    @Query("SELECT * FROM barcode_table ORDER BY id DESC")
    fun getAllBarcodes(): PagingSource<Int, BarcodeData>

    @Query("SELECT * FROM barcode_table ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getBarcodePages(limit: Int, offset: Int): List<BarcodeData>

    @Query("DELETE FROM barcode_table")
    suspend fun deleteAll()

    @Query(
        "SELECT * FROM barcode_table " +
                "WHERE title LIKE :searchQuery " +
                "OR decryptedText LIKE :searchQuery " +
                "ORDER BY id DESC"
    )
    fun searchFromBarcodes(searchQuery: String): PagingSource<Int, BarcodeData>

}