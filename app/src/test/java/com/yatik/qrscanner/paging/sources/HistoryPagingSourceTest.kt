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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import com.google.common.truth.Truth.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.utils.TestConstants.Companion.ITEMS_PER_PAGE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryPagingSourceTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private val mockedDao = mock(BarcodeDao::class.java)
    private lateinit var historyPagingSource: HistoryPagingSource

    private val barcodeData = BarcodeData(
        Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT,
        "sample text", null, null, "09-02-2023 11:50:54"
    )
    private val barcodeData2 = BarcodeData(
        Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT,
        "sample text2", null, null, "09-02-2023 11:50:54"
    )

    @Before
    fun setUp() {
        historyPagingSource = HistoryPagingSource(mockedDao, ITEMS_PER_PAGE)
    }

    @Test
    fun `load returns LoadResult page`() = runTest {
        val mockedList = listOf(barcodeData, barcodeData2)
        Mockito.`when`(mockedDao.getBarcodePages(anyInt(), anyInt())).thenReturn(mockedList)

        val params = PagingSource.LoadParams
            .Refresh<Int>(
                key = null,
                loadSize = ITEMS_PER_PAGE,
                placeholdersEnabled = false
            )
        val result = historyPagingSource.load(params)

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
        val pageResult = result as PagingSource.LoadResult.Page
        assertThat(pageResult.data).isEqualTo(mockedList)
        assertThat(pageResult.prevKey).isNull()
        assertThat(pageResult.nextKey).isEqualTo(1)
    }

}