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

package com.yatik.qrscanner.paging.sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.barcode.BarcodeDetails

class HistoryPagingSource(
    private val barcodeDao: BarcodeDao,
    private val itemsPerPage: Int
) : PagingSource<Int, BarcodeDetails>() {

    /**
     * prevKey == null -> anchorPage is the first page.
     *
     * nextKey == null -> anchorPage is the last page.
     *
     * Both prevKey and nextKey are null -> anchorPage is the initial page, so return null.
     */
    override fun getRefreshKey(state: PagingState<Int, BarcodeDetails>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BarcodeDetails> {
        return try {
            val currentPageNumber = params.key ?: 0
            val barcodeDetailsList =
                barcodeDao.getBarcodePages(itemsPerPage, currentPageNumber * itemsPerPage)
            val prevKey = if (currentPageNumber > 0) currentPageNumber - 1 else null
            val nextKey = if (barcodeDetailsList.isNotEmpty()) currentPageNumber + 1 else null

            LoadResult.Page(
                data = barcodeDetailsList,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}