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

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.yatik.qrscanner.database.converters.BarcodeTypeConverter
import com.yatik.qrscanner.database.converters.NutrimentsTypeConverter
import com.yatik.qrscanner.database.converters.NutriscoreTypeConverter
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.barcode.BarcodeEntity
import com.yatik.qrscanner.models.food.ProductEntity
import com.yatik.qrscanner.utils.mappers.Mapper

@Database(
    version = 5,
    entities = [BarcodeEntity::class, UrlPreviewData::class, ProductEntity::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(
    NutrimentsTypeConverter::class,
    NutriscoreTypeConverter::class,
    BarcodeTypeConverter::class
)
abstract class BarcodeRoomDataBase : RoomDatabase() {

    abstract fun barcodeDao(): BarcodeDao
    abstract fun urlPreviewDao(): UrlPreviewDao
    abstract fun foodDao(): FoodDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.beginTransaction()
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS scanned_data_table (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        details TEXT NOT NULL
                        )
                        """.trimIndent()
                    )

                    val cursor = db.query("SELECT * FROM barcode_table")
                    while (cursor.moveToNext()) {
                        val format = cursor.getInt(cursor.getColumnIndexOrThrow("format"))
                        val type = cursor.getInt(cursor.getColumnIndexOrThrow("type"))
                        val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                        val decryptedText = cursor
                            .getString(cursor.getColumnIndexOrThrow("decryptedText"))
                        val others =
                            cursor.getString(cursor.getColumnIndexOrThrow("others"))
                        val dateTime =
                            cursor.getString(cursor.getColumnIndexOrThrow("dateTime"))
                        val barcodeData =
                            BarcodeData(format, type, title, decryptedText, others, dateTime)
                        val barcodeDetails = Mapper.fromBarcodeDataToBarcodeDetails(barcodeData)
                        val json = Gson().toJson(barcodeDetails)
                        db.execSQL(
                            "INSERT INTO scanned_data_table (details) VALUES (?)",
                            arrayOf(json)
                        )
                    }
                    cursor.close()
                    db.execSQL("DROP TABLE barcode_table")
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

        }
    }

}