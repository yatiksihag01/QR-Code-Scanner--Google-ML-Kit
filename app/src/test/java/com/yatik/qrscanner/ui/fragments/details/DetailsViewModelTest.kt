package com.yatik.qrscanner.ui.fragments.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.yatik.qrscanner.getOrAwaitValueTest
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.TestConstants
import com.yatik.qrscanner.utils.TestConstants.Companion.MAIN_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DetailsViewModel
    private val mockedRepository = mock(DetailsRepository::class.java)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val urlPreviewData = UrlPreviewData(
        MAIN_URL, TestConstants.SUCCESS_RESPONSE_TITLE,
        TestConstants.SUCCESS_RESPONSE_DESCRIPTION, TestConstants.SUCCESS_RESPONSE_IMAGE_URL
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DetailsViewModel(mockedRepository)
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

}