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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.utils.Constants.Companion.ITEMS_PER_PAGE
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalCoroutinesApi::class)
@SmallTest
@HiltAndroidTest
class BarcodeDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var dataBase: BarcodeRoomDataBase
    private lateinit var dao: BarcodeDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = dataBase.barcodeDao()
    }

    @After
    fun tearDown() {
        dataBase.close()
    }

    private val barcodeData = BarcodeData(
        Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample",
        null, null, "27.01.2023 23:41:47"
    )
    private val barcodeData2 = BarcodeData(
        Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample2",
        "https://example.com", null, "28.01.2023 00:00:47"
    )

    @Test
    fun getAllBarcodesTest() = runTest {
        dao.insert(barcodeData)
        dao.insert(barcodeData2)

        val resultPage = dao.getAllBarcodes().load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = ITEMS_PER_PAGE,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        // id is auto-incrementing on actual insertion
        barcodeData.id = 1
        barcodeData2.id = 2

        assertThat(resultPage.data[0]).isEqualTo(barcodeData2)
        assertThat(resultPage.data[1]).isEqualTo(barcodeData)

    }

    @Test
    fun getBarcodePages() = runTest {
        dao.insert(barcodeData2)
        dao.insert(barcodeData2)
        dao.insert(barcodeData)

        dao.insert(barcodeData2)
        dao.insert(barcodeData2)
        dao.insert(barcodeData)
        dao.insert(barcodeData2)
        val initialItemReturned = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Initial",
            null, null, "27.01.2023 23:41:47"
        )
        dao.insert(initialItemReturned) // Because order is DESC by ID

        var barcodesList = dao.getBarcodePages(5, 0)
        assertThat(barcodesList.size).isEqualTo(5)
        assertThat(barcodesList[0].title).isEqualTo(initialItemReturned.title)
        assertThat(barcodesList[4].decryptedText).isEqualTo(barcodeData2.decryptedText)

        barcodesList = dao.getBarcodePages(5, 5)
        assertThat(barcodesList.size).isEqualTo(3)
        assertThat(barcodesList[0].title).isEqualTo(barcodeData.title)
    }

    @Test
    fun deleteAllBarcodeData() = runTest {
        dao.insert(barcodeData)
        dao.insert(barcodeData2)
        dao.deleteAll()

        val resultPage = dao.getAllBarcodes().load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = ITEMS_PER_PAGE,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        assertThat(resultPage.data.size).isEqualTo(0)

    }

    @Test
    fun searchFromBarcodes() = runTest {
        dao.insert(barcodeData)
        dao.insert(barcodeData2)

        var resultPage = dao.searchFromBarcodes("Sample").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = ITEMS_PER_PAGE,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        // id is auto-incrementing on actual insertion
        barcodeData.id = 1
        barcodeData2.id = 2

        assertThat(resultPage.data[0]).isEqualTo(barcodeData)

        resultPage = dao.searchFromBarcodes("https://example.com").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = ITEMS_PER_PAGE,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        assertThat(resultPage.data[0]).isEqualTo(barcodeData2)
    }

}