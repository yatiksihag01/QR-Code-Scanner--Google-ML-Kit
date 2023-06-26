package com.yatik.qrscanner.network.food

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.utils.TestConstants
import com.yatik.qrscanner.utils.TestConstants.Companion.SAMPLE_FOOD_BARCODE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
class FoodApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: FoodApi

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun enqueueMockedResponse(responseCode: Int, responseBody: String) {
        val mockResponse = MockResponse()
            .setResponseCode(responseCode)
            .setBody(responseBody)
        mockWebServer.enqueue(mockResponse)
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(FoodApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `api returns null body for responseCode 404`() = runTest {

        enqueueMockedResponse(TestConstants.ERROR_RESPONSE_CODE, "")
        val response = api.getFoodDetails(SAMPLE_FOOD_BARCODE)
        mockWebServer.takeRequest()

        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(TestConstants.ERROR_RESPONSE_CODE)
        assertThat(response.body()).isNull()
    }

    @Test
    fun `api returns successful response for responseCode 200`() = runTest {

        enqueueMockedResponse(
            TestConstants.SUCCESS_RESPONSE_CODE,
            TestConstants.SUCCESSFUL_FOOD_RESPONSE
        )
        val response = api.getFoodDetails(SAMPLE_FOOD_BARCODE)
        mockWebServer.takeRequest()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.code()).isEqualTo(TestConstants.SUCCESS_RESPONSE_CODE)

        assertThat(response.body()!!.status).isEqualTo(1)
        assertThat(response.body()!!.statusVerbose).isEqualTo("product found")

        assertThat(response.body()!!.product!!.productName).isEqualTo("Biscuit")
        assertThat(response.body()!!.product!!.nutritionGrades).isEqualTo("d")

        assertThat(response.body()!!.product!!.nutriments!!.carbohydrates100g!!.toInt()).isEqualTo(
            54
        )
        assertThat(response.body()!!.product!!.nutriments!!.carbohydratesServing!!.toDouble()).isEqualTo(
            8.1
        )

        assertThat(response.body()!!.product!!.nutriments!!.energyKcal100g!!.toInt()).isEqualTo(544)
        assertThat(response.body()!!.product!!.nutriments!!.energyKcalServing!!.toDouble()).isEqualTo(
            81.6
        )

        assertThat(response.body()!!.product!!.nutriments!!.proteins100g!!.toDouble()).isEqualTo(8.1)
        assertThat(response.body()!!.product!!.nutriments!!.proteinsServing!!.toDouble()).isEqualTo(
            1.22
        )

        assertThat(response.body()!!.product!!.nutriments!!.fat100g!!.toInt()).isEqualTo(32)

        assertThat(response.body()!!.product!!.brands).isEqualTo("Sample brand")
        assertThat(response.body()!!.product!!.quantity).isEqualTo("270 g")
        assertThat(response.body()!!.product!!.frontImageSmall).isEqualTo("https://")
    }

}