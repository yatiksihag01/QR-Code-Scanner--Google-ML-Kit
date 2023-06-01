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