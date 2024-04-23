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

package com.yatik.qrscanner.ui.fragments.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.getOrAwaitValueTest
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.food.Nutriments
import com.yatik.qrscanner.models.food.NutriscoreData
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.repository.details.DetailsRepository
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.TestConstants
import com.yatik.qrscanner.utils.TestConstants.Companion.MAIN_URL
import com.yatik.qrscanner.utils.connectivity.ConnectivityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DetailsViewModel
    private val mockedRepository = mock(DetailsRepository::class.java)
    private val mockedConnectivityHelper = mock(ConnectivityHelper::class.java)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val urlPreviewData = UrlPreviewData(
        MAIN_URL, TestConstants.SUCCESS_RESPONSE_TITLE,
        TestConstants.SUCCESS_RESPONSE_DESCRIPTION, TestConstants.SUCCESS_RESPONSE_IMAGE_URL
    )
    private val product = Product(
        nutriments = Nutriments(energy = 2250),
        nutriscoreData = NutriscoreData(),
        nutritionGrades = "a",
        productName = "Biscuit"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DetailsViewModel(mockedRepository, mockedConnectivityHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getUrlPreview() should update urlPreviewResource`() = runTest(testDispatcher) {

        `when`(mockedRepository.getUrlInfo(MAIN_URL)).thenReturn(
            flowOf(
                Resource.Success(urlPreviewData)
            )
        )
        viewModel.getUrlPreview(MAIN_URL)
        val resource = viewModel.urlPreviewResource.getOrAwaitValueTest()
        assertThat(resource).isInstanceOf(Resource.Success::class.java)
        assertThat(resource.data).isNotNull()
        assertThat(resource.message).isNull()
    }

    @Test
    fun `getFoodDetails() should update foodProductResource`() = runTest(testDispatcher) {

        `when`(mockedRepository.getFoodDetails(anyString())).thenReturn(
            flowOf(Resource.Success(product))
        )
        viewModel.getFoodDetails("123")
        val resource = viewModel.foodProductResource.getOrAwaitValueTest()
        assertThat(resource).isInstanceOf(Resource.Success::class.java)
        assertThat(resource.data!!.productName).isEqualTo("Biscuit")
        assertThat(resource.message).isNull()
    }

}