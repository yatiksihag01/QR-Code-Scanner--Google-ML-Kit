package com.yatik.qrscanner.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.UrlPreviewData

@Database(
    version = 3,
    entities = [BarcodeData::class, UrlPreviewData::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class BarcodeRoomDataBase : RoomDatabase() {

    abstract fun barcodeDao(): BarcodeDao
    abstract fun urlPreviewDao(): UrlPreviewDao

}