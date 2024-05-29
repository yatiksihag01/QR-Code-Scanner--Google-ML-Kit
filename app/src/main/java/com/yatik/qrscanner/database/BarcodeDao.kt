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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.BarcodeEntity

@Dao
interface BarcodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(barcodeEntity: BarcodeEntity)

    @Query("INSERT INTO scanned_data_table (details) VALUES (:barcodeDetails)")
    suspend fun insert(barcodeDetails: String)

    @Query("DELETE FROM scanned_data_table WHERE details = :barcodeEntity")
    suspend fun delete(barcodeEntity: String)

    @Query("SELECT details FROM scanned_data_table ORDER BY id DESC")
    fun getAllBarcodes(): List<BarcodeDetails>

    @Query("SELECT id FROM scanned_data_table WHERE details = :barcodeDetails")
    fun getBarcodeId(barcodeDetails: String): Int

    @Query("SELECT details FROM scanned_data_table ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getBarcodePages(limit: Int, offset: Int): List<BarcodeDetails>

    @Query("DELETE FROM scanned_data_table")
    suspend fun deleteAll()

}