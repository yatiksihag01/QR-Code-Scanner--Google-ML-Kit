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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    fun insertBarcodeData() = runTest {
        val barcodeData = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample",
            null, null, "27.01.2023 23:41:47"
        )
        dao.insert(barcodeData)

        val barcodeData2 = BarcodeData(
            Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT, "Sample2",
            null, null, "28.01.2023 00:00:47"
        )
        launch {
            delay(1000)
            dao.insert(barcodeData2)
        }

        dao.getAllBarcodes().test {

            val barcodeList = awaitItem()
            assertThat(barcodeList).contains(barcodeData)

            val barcodeList2 = awaitItem()
            assertThat(barcodeList2).contains(barcodeData2)
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