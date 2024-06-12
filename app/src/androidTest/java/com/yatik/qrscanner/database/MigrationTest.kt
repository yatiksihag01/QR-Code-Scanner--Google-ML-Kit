/*
 * Copyright 2024 Yatik
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

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration-test"
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private lateinit var helper: MigrationTestHelper

    @Before
    fun setUp() {
        helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            BarcodeRoomDataBase::class.java.canonicalName!!,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        var database = helper.createDatabase(TEST_DB, 4).apply {
           execSQL("INSERT INTO barcode_table VALUES (" +
                   "${Barcode.FORMAT_QR_CODE}," +
                   " ${Barcode.TYPE_TEXT}," +
                   " \"test\"," +
                   " null," +
                   " null," +
                   " \"21-05-2024\"," +
                   " 1" +
                   ")"
           )
            close()
        }
        database = helper.runMigrationsAndValidate(TEST_DB,
            5,
            true,
            BarcodeRoomDataBase.MIGRATION_4_5)
        val cursor = database.query("SELECT * FROM scanned_data_table").apply {
            assertThat(moveToFirst()).isTrue()
            val json = getString(getColumnIndexOrThrow("details"))
            val barcodeDetails = Gson().fromJson(json, BarcodeDetails::class.java)
            assertThat(barcodeDetails.format).isEqualTo(Format.QR_CODE)
            assertThat(barcodeDetails.type).isEqualTo(Type.TYPE_TEXT)
            assertThat(barcodeDetails.rawValue).isEqualTo("test")
            assertThat(barcodeDetails.text).isEqualTo("test")
            assertThat(barcodeDetails.timeStamp).isEqualTo("21-05-2024")
            close()
        }
        cursor.close()
        database.close()
    }

}