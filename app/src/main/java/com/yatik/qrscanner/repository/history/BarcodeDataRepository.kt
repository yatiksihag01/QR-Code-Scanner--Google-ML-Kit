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
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.paging.sources.HistoryPagingSource
import com.yatik.qrscanner.paging.sources.HistorySearchPagingSource
import kotlinx.coroutines.flow.Flow

interface BarcodeDataRepository {

    fun getPagingDataStream(itemsPerPage: Int): Flow<PagingData<BarcodeDetails>>

    fun getSearchedDataStream(searchQuery: String, pageSize: Int): Flow<PagingData<BarcodeDetails>>

    /**
     * Use [BarcodeDataRepository.undoDeletion] if you are inserting to undo deletion as this
     * method does not invalidates the [HistoryPagingSource].
     */
    suspend fun insert(barcodeDetails: BarcodeDetails)

    /**
     * Inserts the given [BarcodeDetails] object into database and invalidates the current
     * [HistoryPagingSource] object returned by
     * [BarcodeDataRepository.getHistoryPagingSource] method.
     */
    suspend fun undoDeletion(barcodeDetails: BarcodeDetails)

    suspend fun delete(barcodeDetails: BarcodeDetails)
    fun getAllBarcodes(): List<BarcodeDetails>

    /**
     * @param itemsPerPage Number of items to be returned per page.
     *
     * @return New instance of [HistoryPagingSource] class.
     */
    fun getHistoryPagingSource(itemsPerPage: Int): HistoryPagingSource

    /**
     * @return New instance of [HistorySearchPagingSource] class
     * if previous instance is null or invalid.
     */
    fun getSearchHistoryPagingSource(pageSize: Int, searchQuery: String): HistorySearchPagingSource
    suspend fun deleteAll()

}