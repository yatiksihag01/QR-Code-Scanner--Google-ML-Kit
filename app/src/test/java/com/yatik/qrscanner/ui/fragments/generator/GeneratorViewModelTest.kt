package com.yatik.qrscanner.ui.fragments.generator

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.yatik.qrscanner.getOrAwaitValueTest
import com.yatik.qrscanner.models.GeneratorData
import com.yatik.qrscanner.repository.GeneratorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class GeneratorViewModelTest {

    private lateinit var fakeGeneratorRepository: GeneratorRepository
    private lateinit var generatorViewModel: GeneratorViewModel
    private val bmp = mock(Bitmap::class.java)
    private lateinit var mockedStaticBmp: MockedStatic<Bitmap>

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
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

        var generatorData = GeneratorData(
            type = Barcode.TYPE_TEXT,
            text = "Test QR code generation"
        )
        val bitMatrix = mock(BitMatrix::class.java)
        val writer = if (generatorData.type == Barcode.TYPE_TEXT) {
            mock(QRCodeWriter::class.java)
        } else {
            mock(MultiFormatWriter::class.java)
        }
        `when`(Bitmap.createBitmap(anyInt(), anyInt(), any())).thenReturn(bmp)
        `when`(writer.encode(anyString(), any(), anyInt(), anyInt())).thenReturn(bitMatrix)

        generatorViewModel.generateQRCode(generatorData)
        var bitmap = generatorViewModel.bitmap.getOrAwaitValueTest()
        assertThat(bitmap).isNotNull()
        assertThat(bitmap).isEqualTo(bmp)
        assertThat(
            generatorViewModel.isQRGeneratedSuccessfully.getOrAwaitValueTest()
        ).isTrue()

        generatorData = GeneratorData(
            type = Barcode.TYPE_WIFI,
            ssid = "Test",
            password = "test",
            securityType = "WPA"
        )
        generatorViewModel.generateQRCode(generatorData)
        bitmap = generatorViewModel.bitmap.getOrAwaitValueTest()
        assertThat(bitmap).isNotNull()
        assertThat(bitmap).isEqualTo(bmp)

    }

    @Test
    fun `generateQRCode should set isQRGeneratedSuccessfully false for WriterException`() =
        runTest {

            val generatorData = GeneratorData(
                type = Barcode.TYPE_TEXT,
                text = "Test QR code generation"
            )
            val writer = if (generatorData.type == Barcode.TYPE_TEXT) {
                mock(QRCodeWriter::class.java)
            } else {
                mock(MultiFormatWriter::class.java)
            }
            `when`(Bitmap.createBitmap(anyInt(), anyInt(), any())).thenReturn(bmp)
            `when`(
                writer.encode(
                    anyString(),
                    any(),
                    anyInt(),
                    anyInt()
                )
            ).thenThrow(WriterException())
            generatorViewModel.generateQRCode(generatorData)
            assertThat(
                generatorViewModel.isQRGeneratedSuccessfully.getOrAwaitValueTest()
            ).isFalse()

        }

    @Test
    fun `generateQRCode should set isQRGeneratedSuccessfully false for IllegalArgumentException`() =
        runTest {

            val generatorData = GeneratorData(
                type = Barcode.TYPE_TEXT,
                text = "Test QR code generation"
            )
            val writer = if (generatorData.type == Barcode.TYPE_TEXT) {
                mock(QRCodeWriter::class.java)
            } else {
                mock(MultiFormatWriter::class.java)
            }
            val bitMatrix = mock(BitMatrix::class.java)
            `when`(writer.encode(anyString(), any(), anyInt(), anyInt())).thenReturn(bitMatrix)
            `when`(
                Bitmap.createBitmap(
                    anyInt(),
                    anyInt(),
                    any()
                )
            ).thenThrow(java.lang.IllegalArgumentException())
            generatorViewModel.generateQRCode(generatorData)
            assertThat(
                generatorViewModel.isQRGeneratedSuccessfully.getOrAwaitValueTest()
            ).isFalse()

        }

    @Test
    fun `saveImageToGallery should set imageSaved to true on successful insertion`() = runTest {

        val generatorData = GeneratorData(
            type = Barcode.TYPE_TEXT,
            text = "Test QR code generation"
        )
        val bitMatrix = mock(BitMatrix::class.java)
        val writer = mock(QRCodeWriter::class.java)

        `when`(Bitmap.createBitmap(anyInt(), anyInt(), any())).thenReturn(bmp)
        `when`(writer.encode(anyString(), any(), anyInt(), anyInt())).thenReturn(bitMatrix)
        `when`(fakeGeneratorRepository.saveImageToGallery(bmp)).thenReturn(true)

        generatorViewModel.generateQRCode(generatorData)
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