package com.yatik.qrscanner.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yatik.qrscanner.database.converters.NutrimentsTypeConverter
import com.yatik.qrscanner.database.converters.NutriscoreTypeConverter
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.food.ProductEntity

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

@Database(
    version = 4,
    entities = [BarcodeData::class, UrlPreviewData::class, ProductEntity::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
@TypeConverters(
    NutrimentsTypeConverter::class,
    NutriscoreTypeConverter::class
)
abstract class BarcodeRoomDataBase : RoomDatabase() {

    abstract fun barcodeDao(): BarcodeDao
    abstract fun urlPreviewDao(): UrlPreviewDao
    abstract fun foodDao(): FoodDao

}