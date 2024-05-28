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

package com.yatik.qrscanner.ui.fragments.generator

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.yatik.qrscanner.getOrAwaitValueTest
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.data.Security
import com.yatik.qrscanner.models.barcode.data.WiFi
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import com.yatik.qrscanner.repository.barcode_generator.GeneratorRepository
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
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class GeneratorViewModelTest {

    private lateinit var fakeGeneratorRepository: GeneratorRepository
    private lateinit var generatorViewModel: GeneratorViewModel
    private val bmp = mock(Bitmap::class.java)
    private lateinit var mockedStaticBmp: MockedStatic<Bitmap>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeGeneratorRepository = mock(GeneratorRepository::class.java)
        generatorViewModel = GeneratorViewModel(fakeGeneratorRepository)
        mockedStaticBmp = mockStatic(Bitmap::class.java)
    }

    @After
    fun tearDown() {
        mockedStaticBmp.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `generateQRCode should update bitmap and isQRGeneratedSuccessfully`() = runTest {

        `when`(Bitmap.createBitmap(anyInt(), anyInt(), any())).thenReturn(bmp)

        val ssid = "Test"
        val password = "test"
        val security = Security.WPA

        val barcodeDetails = BarcodeDetails(
            format = Format.QR_CODE,
            type = Type.TYPE_WIFI,
            null,
            "$ssid, $password, $security",
            wiFi = WiFi(ssid, password, security)
        )
        generatorViewModel.generateBarcode(barcodeDetails)
        val bitmap = generatorViewModel.bitmap.getOrAwaitValueTest()
        assertThat(bitmap).isNotNull()
        assertThat(bitmap).isEqualTo(bmp)
        assertThat(
            generatorViewModel.isQRGeneratedSuccessfully.getOrAwaitValueTest()
        ).isTrue()

    }

    @Test
    fun `generateQRCode should set isQRGeneratedSuccessfully false for Exceptions`() =
        runTest {

            val barcodeDetails = BarcodeDetails(
                format = Format.QR_CODE,
                type = Type.TYPE_TEXT,
                null,
                "test",
                text = "test"
            )

            val writer = mock(MultiFormatWriter::class.java)
            val bitMatrix = mock(BitMatrix::class.java)

            `when`(writer.encode(anyString(), any(), anyInt(), anyInt())).thenReturn(bitMatrix)
            `when`(
                Bitmap.createBitmap(
                    anyInt(),
                    anyInt(),
                    any()
                )
            ).thenThrow(java.lang.IllegalArgumentException())
            generatorViewModel.generateBarcode(barcodeDetails)
            assertThat(
                generatorViewModel.isQRGeneratedSuccessfully.getOrAwaitValueTest()
            ).isFalse()

        }

    @Test
    fun `saveImageToGallery should set imageSaved to true on successful insertion`() = runTest {

        val barcodeDetails = BarcodeDetails(
            format = Format.QR_CODE,
            type = Type.TYPE_TEXT,
            null,
            "Test",
            text = "Test"
        )

        `when`(Bitmap.createBitmap(anyInt(), anyInt(), any())).thenReturn(bmp)
        `when`(fakeGeneratorRepository.saveImageToGallery(bmp)).thenReturn(true)

        generatorViewModel.generateBarcode(barcodeDetails)
        generatorViewModel.saveImageToGallery()
        assertThat(
            generatorViewModel.imageSaved.getOrAwaitValueTest()
        ).isTrue()

    }

    @Test
    fun `saveImageToGallery should set imageSaved to false if bitmap is null`() = runTest {

        `when`(fakeGeneratorRepository.saveImageToGallery(bmp)).thenReturn(true)
        generatorViewModel.saveImageToGallery()
        assertThat(
            generatorViewModel.imageSaved.getOrAwaitValueTest()
        ).isFalse()

    }

    @Test
    fun `saveImageToGallery should set imageSaved to false if image is not saved`() = runTest {

        `when`(fakeGeneratorRepository.saveImageToGallery(bmp)).thenReturn(false)
        generatorViewModel.saveImageToGallery()
        assertThat(
            generatorViewModel.imageSaved.getOrAwaitValueTest()
        ).isFalse()

    }

}