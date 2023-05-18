package com.yatik.qrscanner.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.models.BarcodeData
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

        dao.getAllBarcodes().test {

            val barcodeList = awaitItem()
            var data = barcodeList[1] // items returned by getAllBarcodes() are in LIFO order

            // id = 0 means auto-generation not working properly
            assertThat(data.id).isNotEqualTo(0)
            assertThat(data.format).isEqualTo(barcodeData.format)
            assertThat(data.title).isEqualTo(barcodeData.title)
            assertThat(data.dateTime).isEqualTo(barcodeData.dateTime)

            data = barcodeList[0]
            assertThat(data.format).isEqualTo(barcodeData2.format)
            assertThat(data.title).isEqualTo(barcodeData2.title)
            assertThat(data.dateTime).isEqualTo(barcodeData2.dateTime)
            cancel()
        }
    }

    @Test
    fun deleteSingleBarcodeData() = runTest {

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

        dao.getAllBarcodes().test {
            val barcodesList = awaitItem()
            // Delete the barcodeData returned from the list, because it will have an ID
            dao.delete(barcodesList[1])

            val barcodesList2 = awaitItem()
            assertThat(barcodesList2).doesNotContain(barcodeData)
            assertThat(barcodesList2.size).isEqualTo(1)

            cancel()
        }

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
        dao.getAllBarcodes().test {
            val barcodesList = awaitItem()
            assertThat(barcodesList.size).isEqualTo(0)
            cancel()
        }

    }

}