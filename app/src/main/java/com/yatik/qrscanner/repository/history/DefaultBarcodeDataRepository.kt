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
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.BarcodeEntity
import com.yatik.qrscanner.paging.sources.HistoryPagingSource
import com.yatik.qrscanner.paging.sources.HistorySearchPagingSource
import com.yatik.qrscanner.utils.Constants
import com.yatik.qrscanner.utils.mappers.Mapper
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
    private var searchHistoryPagingSource: HistorySearchPagingSource? = null
    private var deletedBarcodeId: Int? = null

    override fun getPagingDataStream(itemsPerPage: Int): Flow<PagingData<BarcodeDetails>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.ITEMS_PER_PAGE)
        ) { getHistoryPagingSource(Constants.ITEMS_PER_PAGE) }
            .flow
    }

    override fun getSearchedDataStream(
        searchQuery: String,
        pageSize: Int
    ): Flow<PagingData<BarcodeDetails>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize)
        ) { getSearchHistoryPagingSource(pageSize, searchQuery) }
            .flow
    }

    override suspend fun insert(barcodeDetails: BarcodeDetails) = withContext(ioDispatcher) {
        barcodeDao.insert(Mapper.fromBarcodeDetailsToJson(barcodeDetails))
    }

    override suspend fun undoDeletion(barcodeDetails: BarcodeDetails) = withContext(ioDispatcher) {
        // This workaround is to ensure that deleted item is restored at same location in table
        // i.e. in same position as it was deleted
        val entity = BarcodeEntity(
            id = deletedBarcodeId!!,
            barcodeDetails = Mapper.fromBarcodeDetailsToJson(barcodeDetails)
        )
        barcodeDao.insert(entity)
        searchHistoryPagingSource?.invalidate()
        historyPagingSource!!.invalidate()
    }

    override suspend fun delete(barcodeDetails: BarcodeDetails) = withContext(ioDispatcher) {
        val json = Mapper.fromBarcodeDetailsToJson(barcodeDetails)
        deletedBarcodeId = barcodeDao.getBarcodeId(json)
        barcodeDao.delete(json)
        searchHistoryPagingSource?.invalidate()
        historyPagingSource!!.invalidate()
    }

    override fun getAllBarcodes(): List<BarcodeDetails> {
        return barcodeDao.getAllBarcodes()
    }


    override suspend fun deleteAll() = withContext(ioDispatcher) {
        barcodeDao.deleteAll()
        searchHistoryPagingSource?.invalidate()
        historyPagingSource!!.invalidate()
    }

    override fun getHistoryPagingSource(itemsPerPage: Int): HistoryPagingSource {
        if (historyPagingSource == null || historyPagingSource?.invalid == true) {
            historyPagingSource = HistoryPagingSource(barcodeDao, itemsPerPage)
        }
        return historyPagingSource!!
    }

    override fun getSearchHistoryPagingSource(
        pageSize: Int,
        searchQuery: String
    ): HistorySearchPagingSource {
        if (searchHistoryPagingSource == null || searchHistoryPagingSource?.invalid == true) {
            searchHistoryPagingSource = HistorySearchPagingSource(barcodeDao, pageSize)
        }
        searchHistoryPagingSource!!.searchQuery = searchQuery
        return searchHistoryPagingSource!!
    }

}