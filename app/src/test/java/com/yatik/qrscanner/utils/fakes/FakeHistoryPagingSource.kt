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

package com.yatik.qrscanner.utils.fakes

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yatik.qrscanner.models.BarcodeData

class FakeHistoryPagingSource(
    private val barcodeDataList: List<BarcodeData>
) : PagingSource<Int, BarcodeData>() {

    override fun getRefreshKey(state: PagingState<Int, BarcodeData>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BarcodeData> {
        return LoadResult.Page(
            data = barcodeDataList,
            prevKey = null,
            nextKey = null
        )
    }
}