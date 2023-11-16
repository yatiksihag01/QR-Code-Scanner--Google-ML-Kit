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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.paging.sources.HistoryPagingSource
import com.yatik.qrscanner.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultBarcodeDataRepository @Inject constructor(
    private val barcodeDao: BarcodeDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BarcodeDataRepository {

    private var historyPagingSource: HistoryPagingSource? = null

    override fun getPagingDataStream(itemsPerPage: Int): Flow<PagingData<BarcodeData>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.ITEMS_PER_PAGE)
        ) { getHistoryPagingSource(Constants.ITEMS_PER_PAGE) }
            .flow
    }

    override suspend fun insert(barcodeData: BarcodeData) = withContext(ioDispatcher) {
        barcodeDao.insert(barcodeData)
    }

    override suspend fun undoDeletion(barcodeData: BarcodeData) {
        barcodeDao.insert(barcodeData)
        historyPagingSource!!.invalidate()
    }

    override suspend fun delete(barcodeData: BarcodeData) = withContext(ioDispatcher) {
        barcodeDao.delete(barcodeData)
        historyPagingSource!!.invalidate()
    }

    override fun getAllBarcodes(): PagingSource<Int, BarcodeData> {
        return barcodeDao.getAllBarcodes()
    }

    override fun getHistoryPagingSource(itemsPerPage: Int): HistoryPagingSource {
        if (historyPagingSource == null || historyPagingSource?.invalid == true) {
            historyPagingSource = HistoryPagingSource(barcodeDao, itemsPerPage)
        }
        return historyPagingSource!!
    }

    override suspend fun deleteAll() = withContext(ioDispatcher) {
        barcodeDao.deleteAll()
        historyPagingSource!!.invalidate()
    }

    override fun searchFromBarcodes(searchQuery: String): Flow<PagingData<BarcodeData>> {
        val formattedQuery =
            if (!searchQuery.startsWith("%") && !searchQuery.endsWith("%")) {
                "%${searchQuery}%"
            } else searchQuery

        return Pager(
            config = PagingConfig(pageSize = Constants.ITEMS_PER_PAGE)
        ) { barcodeDao.searchFromBarcodes(formattedQuery) }
            .flow
    }

}