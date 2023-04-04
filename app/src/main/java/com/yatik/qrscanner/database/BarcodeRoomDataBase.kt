package com.yatik.qrscanner.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.yatik.qrscanner.models.BarcodeData

@Database(
    version = 2,
    entities = [BarcodeData::class],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class BarcodeRoomDataBase : RoomDatabase() {
    abstract fun barcodeDao(): BarcodeDao

}