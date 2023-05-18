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

    @Test
    fun getAllBarcodesTest() = runTest {
        val barcodeData = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample",
            null, null, "27.01.2023 23:41:47"
        )
        val barcodeData2 = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample2",
            null, null, "28.01.2023 00:00:47"
        )
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
    fun deleteAllBarcodeData() = runTest {

        val barcodeData = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample",
            null, null, "27.01.2023 23:41:47"
        )
        dao.insert(barcodeData)

        val barcodeData2 = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample2",
            null, null, "28.01.2023 00:00:47"
        )
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

}