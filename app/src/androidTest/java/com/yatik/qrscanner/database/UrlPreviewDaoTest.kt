package com.yatik.qrscanner.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.models.UrlPreviewData
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalCoroutinesApi::class)
@SmallTest
@HiltAndroidTest
class UrlPreviewDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var dataBase: BarcodeRoomDataBase
    private lateinit var dao: UrlPreviewDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = dataBase.urlPreviewDao()
    }

    @After
    fun tearDown() {
        dataBase.close()
    }

    @Test
    fun getUrlInfoTest() = runTest {

        val mainUrl = "https://www.testurl.com"
        val urlPreviewData = UrlPreviewData(
            mainUrl,
            "Test Title",
            "Test description",
            "https://www.testurl.com/home/image"
        )
        dao.upsertUrlInfo(urlPreviewData)

        assertThat(dao.getUrlInfo(mainUrl)).isNotNull()
        assertThat(dao.getUrlInfo(mainUrl)).isEqualTo(urlPreviewData)
        assertThat(dao.getUrlInfo("https://www.anotherurl.com/")).isNull()
    }

}