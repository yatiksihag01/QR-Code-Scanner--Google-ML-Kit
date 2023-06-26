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