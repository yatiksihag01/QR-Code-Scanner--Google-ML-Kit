package com.yatik.qrscanner.repository.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.yatik.qrscanner.database.FoodDao
import com.yatik.qrscanner.database.UrlPreviewDao
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.food.FoodResponse
import com.yatik.qrscanner.models.food.Nutriments
import com.yatik.qrscanner.models.food.NutriscoreData
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.models.food.ProductEntity
import com.yatik.qrscanner.network.UrlPreviewApi
import com.yatik.qrscanner.network.food.FoodApi
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.TestConstants.Companion.ERROR_RESPONSE_CODE
import com.yatik.qrscanner.utils.TestConstants.Companion.MAIN_URL
import com.yatik.qrscanner.utils.TestConstants.Companion.RESPONSE_ON_ERROR_CODE
import com.yatik.qrscanner.utils.TestConstants.Companion.SUCCESS_RESPONSE
import com.yatik.qrscanner.utils.TestConstants.Companion.SUCCESS_RESPONSE_DESCRIPTION
import com.yatik.qrscanner.utils.TestConstants.Companion.SUCCESS_RESPONSE_IMAGE_URL
import com.yatik.qrscanner.utils.TestConstants.Companion.SUCCESS_RESPONSE_TITLE
import com.yatik.qrscanner.utils.connectivity.ConnectivityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response
import java.io.IOException

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

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultDetailsRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private val mockedDao = mock(UrlPreviewDao::class.java)
    private val mockedApi = mock(UrlPreviewApi::class.java)
    private val mockedFoodDao = mock(FoodDao::class.java)
    private val mockedFoodApi = mock(FoodApi::class.java)
    private val mockedConnectivityHelper = mock(ConnectivityHelper::class.java)

    private lateinit var repository: DefaultDetailsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = DefaultDetailsRepository(
            mockedApi,
            mockedDao, mockedFoodDao,
            mockedFoodApi, mockedConnectivityHelper,
            testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getData() returns two Loadings and then Success when remote data is successfully retrieved`() =
        runTest(testDispatcher) {

            val responseBody = SUCCESS_RESPONSE.toResponseBody("text/plain".toMediaTypeOrNull())
            val response = Response.success(responseBody)

            val urlPreviewData = UrlPreviewData(
                MAIN_URL, SUCCESS_RESPONSE_TITLE,
                SUCCESS_RESPONSE_DESCRIPTION, SUCCESS_RESPONSE_IMAGE_URL
            )

            `when`(mockedConnectivityHelper.isConnectedToInternet()).thenReturn(true)
            `when`(mockedApi.getUrlPreview(MAIN_URL)).thenReturn(response)
            `when`(mockedDao.getUrlInfo(MAIN_URL)).thenReturn(urlPreviewData)

            `when`(mockedDao.deleteUrlInfo(urlPreviewData)).thenAnswer { }
            `when`(mockedDao.upsertUrlInfo(urlPreviewData)).thenAnswer { }

            repository.getUrlInfo(MAIN_URL).test {

                val emptyLoading = awaitItem()
                assertThat(emptyLoading).isInstanceOf(Resource.Loading::class.java)
                assertThat(emptyLoading.data).isNull()
                assertThat(emptyLoading.message).isNull()

                val loadingWithData = awaitItem()
                assertThat(loadingWithData).isInstanceOf(Resource.Loading::class.java)
                assertThat(loadingWithData.data?.title).isEqualTo(SUCCESS_RESPONSE_TITLE)
                assertThat(loadingWithData.data?.description).isEqualTo(SUCCESS_RESPONSE_DESCRIPTION)
                assertThat(loadingWithData.data?.imageUrl).isEqualTo(SUCCESS_RESPONSE_IMAGE_URL)

                val lastEmit = awaitItem()
                assertThat(lastEmit).isInstanceOf(Resource.Success::class.java)
                assertThat(loadingWithData.data?.title).isEqualTo(SUCCESS_RESPONSE_TITLE)
                assertThat(loadingWithData.data?.description).isEqualTo(SUCCESS_RESPONSE_DESCRIPTION)
                assertThat(loadingWithData.data?.imageUrl).isEqualTo(SUCCESS_RESPONSE_IMAGE_URL)

                awaitComplete()
                cancel()
            }
        }

    @Test
    fun `getData() returns two Loadings and one Error for server error and no internet`() =
        runTest(testDispatcher) {

            val urlPreviewData = UrlPreviewData(
                MAIN_URL, SUCCESS_RESPONSE_TITLE,
                SUCCESS_RESPONSE_DESCRIPTION, SUCCESS_RESPONSE_IMAGE_URL
            )

            val responseBody =
                RESPONSE_ON_ERROR_CODE.toResponseBody("text/plain".toMediaTypeOrNull())
            val response = Response.error<ResponseBody>(ERROR_RESPONSE_CODE, responseBody)

            `when`(mockedConnectivityHelper.isConnectedToInternet()).thenReturn(true)
            `when`(mockedApi.getUrlPreview(MAIN_URL)).thenReturn(response)
            `when`(mockedDao.getUrlInfo(MAIN_URL)).thenReturn(urlPreviewData)

            `when`(mockedDao.deleteUrlInfo(urlPreviewData)).thenAnswer { }
            `when`(mockedDao.upsertUrlInfo(urlPreviewData)).thenAnswer { }

            repository.getUrlInfo(MAIN_URL).test {

                val emptyLoading = awaitItem()
                assertThat(emptyLoading).isInstanceOf(Resource.Loading::class.java)
                assertThat(emptyLoading.data).isNull()
                assertThat(emptyLoading.message).isNull()

                val loadingWithData = awaitItem()
                assertThat(loadingWithData).isInstanceOf(Resource.Loading::class.java)
                assertThat(loadingWithData.data?.title).isEqualTo(SUCCESS_RESPONSE_TITLE)
                assertThat(loadingWithData.data?.description).isEqualTo(SUCCESS_RESPONSE_DESCRIPTION)
                assertThat(loadingWithData.data?.imageUrl).isEqualTo(SUCCESS_RESPONSE_IMAGE_URL)

                val errorWithMessage = awaitItem()
                assertThat(errorWithMessage).isInstanceOf(Resource.Error::class.java)
                assertThat(errorWithMessage.data).isNull()
                assertThat(errorWithMessage.message).isNotEmpty()

                awaitComplete()
                cancel()
            }
        }

    @Test
    fun `getData() catches api exceptions`() = runTest(testDispatcher) {

        val urlPreviewData = UrlPreviewData(
            MAIN_URL, SUCCESS_RESPONSE_TITLE,
            SUCCESS_RESPONSE_DESCRIPTION, SUCCESS_RESPONSE_IMAGE_URL
        )

        `when`(mockedConnectivityHelper.isConnectedToInternet()).thenReturn(true)
        `when`(mockedApi.getUrlPreview(MAIN_URL)).thenAnswer { throw IOException() }
        `when`(mockedDao.getUrlInfo(MAIN_URL)).thenReturn(urlPreviewData)

        `when`(mockedDao.deleteUrlInfo(urlPreviewData)).thenAnswer { }
        `when`(mockedDao.upsertUrlInfo(urlPreviewData)).thenAnswer { }

        repository.getUrlInfo(MAIN_URL).test {

            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)

            val errorEmit = awaitItem()
            assertThat(errorEmit).isInstanceOf(Resource.Error::class.java)
            assertThat(errorEmit.message).isNotEmpty()

            awaitComplete()
            cancel()
        }
    }

    @Test
    fun `getFoodDetails() returns two Loadings and then Success when remote data is successfully retrieved`() =
        runTest(testDispatcher) {

            val barcodeString = "123"
            val product = Product(
                nutriments = Nutriments(energy = 2250),
                nutriscoreData = NutriscoreData(),
                nutritionGrades = "a",
                productName = "pBiscuit",
                frontImageSmall = "https://openfoodfacts.org/sample_image",
                brands = "Sample brand",
                quantity = "250 grams"
            )
            val productEntity = ProductEntity(
                nutriments = Nutriments(energy = 3000),
                nutriscoreData = product.nutriscoreData,
                nutritionGrades = product.nutritionGrades,
                productName = "pEBiscuit",
                frontImageSmall = "https://openfoodfacts.org/sample_image",
                brands = "Sample brand",
                quantity = "250 grams",
                barcode = barcodeString,
                timestamp = 123456789123
            )

            val responseBody = FoodResponse(
                code = barcodeString,
                product = product,
                status = 1,
                statusVerbose = "product found"
            )
            val response = Response.success(responseBody)

            `when`(mockedConnectivityHelper.isConnectedToInternet()).thenReturn(true)
            `when`(mockedFoodApi.getFoodDetails(anyString(), anyString())).thenReturn(response)
            `when`(mockedFoodDao.getProduct(anyString())).thenReturn(productEntity)
            `when`(mockedFoodDao.upsertProduct(productEntity)).thenAnswer { }

            repository.getFoodDetails(barcodeString).test {

                val emptyLoading = awaitItem()
                assertThat(emptyLoading).isInstanceOf(Resource.Loading::class.java)
                assertThat(emptyLoading.data).isNull()
                assertThat(emptyLoading.message).isNull()

                val dataLoading = awaitItem()
                assertThat(dataLoading).isInstanceOf(Resource.Loading::class.java)
                assertThat(dataLoading.data!!.productName).isEqualTo("pEBiscuit")

                val dataSuccess = awaitItem()
                assertThat(dataSuccess.data!!.nutriments!!.energy!!.toInt()).isEqualTo(2250)
                assertThat(dataSuccess.data!!.productName).isEqualTo("pBiscuit")

                awaitComplete()
                cancel()
            }

        }

}