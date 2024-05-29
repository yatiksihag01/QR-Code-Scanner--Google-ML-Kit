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

package com.yatik.qrscanner.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.data.Url
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import com.yatik.qrscanner.utils.mappers.Mapper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@SmallTest
@HiltAndroidTest
class BarcodeDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var dataBase: BarcodeRoomDataBase
    private lateinit var dao: BarcodeDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = dataBase.barcodeDao()
    }

    @After
    fun tearDown() {
        dataBase.close()
    }

    private val barcodeDetails = BarcodeDetails(
        Format.QR_CODE,
        Type.TYPE_TEXT,
        "27.01.2023 23:41:47", "Sample",
        text = "Sample"
    )
    private val barcodeDetails2 = BarcodeDetails(
        Format.QR_CODE, Type.TYPE_URL, "27.01.2023 23:41:47",
        "https://example.com", url = Url("SampleUrl", "https://example.com")
    )
    private val json = Mapper.fromBarcodeDetailsToJson(barcodeDetails)
    private val json2 = Mapper.fromBarcodeDetailsToJson(barcodeDetails2)

    @Test
    fun checkTypeConverter() = runTest {
        dao.insert(json)
        dao.insert(json2)

        val barcodesList = dao.getAllBarcodes()
        assertThat(barcodesList[0]).isInstanceOf(BarcodeDetails::class.java)
    }

    @Test
    fun getBarcodePagesTest() = runTest {
        dao.insert(json)
        dao.insert(json2)
        dao.insert(json)

        dao.insert(json2)
        dao.insert(json)
        dao.insert(json2)
        dao.insert(json)

        var barcodesList = dao.getBarcodePages(5, 0)
        assertThat(barcodesList.size).isEqualTo(5)
        assertThat(barcodesList[4].type).isEqualTo(barcodeDetails.type)

        barcodesList = dao.getBarcodePages(5, 5)
        assertThat(barcodesList.size).isEqualTo(2)
        assertThat(barcodesList[1].timeStamp).isEqualTo(barcodeDetails2.timeStamp)
    }

}