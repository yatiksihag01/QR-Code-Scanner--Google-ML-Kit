package com.yatik.qrscanner.repository.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.*
import com.yatik.qrscanner.database.UrlPreviewDao
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.network.UrlPreviewApi
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultDetailsRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()

    private val mockedDao = mock(UrlPreviewDao::class.java)
    private val mockedApi = mock(UrlPreviewApi::class.java)
    private val mockedConnectivityHelper = mock(ConnectivityHelper::class.java)

    private lateinit var repository: DefaultDetailsRepository


    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = DefaultDetailsRepository(
            mockedApi,
            mockedDao, mockedConnectivityHelper,
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
    fun `getData() returns two Loadings for server error and no internet`() =
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

}