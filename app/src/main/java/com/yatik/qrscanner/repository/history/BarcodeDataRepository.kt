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

package com.yatik.qrscanner.repository.history

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.paging.sources.HistoryPagingSource
import kotlinx.coroutines.flow.Flow

interface BarcodeDataRepository {

    fun getPagingDataStream(itemsPerPage: Int): Flow<PagingData<BarcodeData>>

    /**
     * Use [BarcodeDataRepository.undoDeletion] if you are inserting to undo deletion as this
     * method does not invalidates the [HistoryPagingSource].
     */
    suspend fun insert(barcodeData: BarcodeData)

    /**
     * Inserts the given [BarcodeData] object into database and invalidates the current
     * [HistoryPagingSource] object returned by
     * [BarcodeDataRepository.getHistoryPagingSource] method.
     */
    suspend fun undoDeletion(barcodeData: BarcodeData)

    suspend fun delete(barcodeData: BarcodeData)
    fun getAllBarcodes(): PagingSource<Int, BarcodeData>

    /**
     * @param itemsPerPage Number of items to be returned per page.
     *
     * @return New instance of [HistoryPagingSource] class.
     */
    fun getHistoryPagingSource(itemsPerPage: Int): HistoryPagingSource
    suspend fun deleteAll()
    fun searchFromBarcodes(searchQuery: String): Flow<PagingData<BarcodeData>>

}