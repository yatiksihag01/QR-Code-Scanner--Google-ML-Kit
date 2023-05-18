package com.yatik.qrscanner.ui.fragments.history

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import com.google.common.truth.Truth.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.getOrAwaitValueTest
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.repository.FakeBarcodeDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val fakeRepository = FakeBarcodeDataRepository()
    private lateinit var viewModel: BarcodeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    val barcodeData = BarcodeData(
        Barcode.FORMAT_QR_CODE, Barcode.TYPE_TEXT,
        "sample text", null, null, "09-02-2023 11:50:54"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = BarcodeViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert single barcodeData, returns size 1`() = runTest {

        viewModel.insert(barcodeData)
        val barcodeDataList = fakeRepository.getAllBarcodes()
            .asLiveData()
            .getOrAwaitValueTest()
        assertThat(barcodeDataList.size).isEqualTo(1)
    }

    @Test
    fun `insert two barcodeData, returns size 2`() = runTest {

        viewModel.insert(barcodeData)
        val barcodeDataList = fakeRepository.getAllBarcodes()
            .asLiveData()
            .getOrAwaitValueTest()
        assertThat(barcodeDataList.size).isEqualTo(1)

    }

    @Test
    fun `insert single barcodeData, returns same barcodeData`() = runTest {

        viewModel.insert(barcodeData)

        val barcodeDataList = fakeRepository.getAllBarcodes()
            .asLiveData()
            .getOrAwaitValueTest()
        assertThat(barcodeDataList[0]).isEqualTo(barcodeData)

    }

    @Test
    fun `delete last barcodeData, returns size 0`() = runTest {

        viewModel.insert(barcodeData)
        viewModel.delete(barcodeData)

        val barcodeDataList = fakeRepository.getAllBarcodes()
            .asLiveData()
            .getOrAwaitValueTest()
        assertThat(barcodeDataList.size).isEqualTo(0)

    }

    @Test
    fun `delete all barcodeData, returns size 0`() = runTest {

        viewModel.insert(barcodeData)
        viewModel.deleteAll()

        val barcodeDataList = fakeRepository.getAllBarcodes()
            .asLiveData()
            .getOrAwaitValueTest()
        assertThat(barcodeDataList.size).isEqualTo(0)
    }

    @Test
    fun `getAllBarcodes() returns BarcodeData list of size one`() = runTest {

        fakeRepository.insert(barcodeData)

        val barcodeDataList = viewModel.getAllBarcodes()
            .getOrAwaitValueTest()
        assertThat(barcodeDataList.size).isEqualTo(1)
    }

}