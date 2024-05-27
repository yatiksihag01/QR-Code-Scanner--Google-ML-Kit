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
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.data.Url
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import com.yatik.qrscanner.utils.TestConstants.Companion.ITEMS_PER_PAGE
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.mock

class HistoryPagingSourceTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private val mockedDao = mock(BarcodeDao::class.java)
    private lateinit var historyPagingSource: HistoryPagingSource

    private val barcodeDetails = BarcodeDetails(
        Format.QR_CODE,
        Type.TYPE_TEXT,
        "27.01.2023 23:41:47", "Sample",
        text = "Sample"
    )
    private val barcodeDetails2 = BarcodeDetails(
        Format.QR_CODE, Type.TYPE_URL, "27.01.2023 23:41:47",
        "https://example.com", url = Url("SampleUrl", "https://example.com")
    )

    @Before
    fun setUp() {
        historyPagingSource = HistoryPagingSource(mockedDao, ITEMS_PER_PAGE)
    }

    @Test
    fun `load returns LoadResult page`() = runTest {
        val mockedList = listOf(barcodeDetails, barcodeDetails2)
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