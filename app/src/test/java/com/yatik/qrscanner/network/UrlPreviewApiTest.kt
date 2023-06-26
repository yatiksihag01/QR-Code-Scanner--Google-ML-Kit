package com.yatik.qrscanner.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.utils.TestConstants.Companion.ERROR_RESPONSE_CODE
import com.yatik.qrscanner.utils.TestConstants.Companion.RESPONSE_ON_ERROR_CODE
import com.yatik.qrscanner.utils.TestConstants.Companion.SUCCESS_RESPONSE
import com.yatik.qrscanner.utils.TestConstants.Companion.SUCCESS_RESPONSE_CODE
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
class UrlPreviewApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: UrlPreviewApi
    private lateinit var testUrl: String

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
        testUrl = mockWebServer.url("/").toString()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(UrlPreviewApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `api returns error response for responseCode 404`() = runTest {

        enqueueMockedResponse(ERROR_RESPONSE_CODE, RESPONSE_ON_ERROR_CODE)
        val response = api.getUrlPreview(testUrl)
        mockWebServer.takeRequest()

        assertThat(response.isSuccessful).isFalse()
        assertThat(response.code()).isEqualTo(ERROR_RESPONSE_CODE)
    }

    @Test
    fun `api returns successful response for responseCode 200`() = runTest {

        enqueueMockedResponse(SUCCESS_RESPONSE_CODE, SUCCESS_RESPONSE)
        val response = api.getUrlPreview(testUrl)
        mockWebServer.takeRequest()

        assertThat(response.isSuccessful).isTrue()
        assertThat(response.code()).isEqualTo(SUCCESS_RESPONSE_CODE)
    }
}