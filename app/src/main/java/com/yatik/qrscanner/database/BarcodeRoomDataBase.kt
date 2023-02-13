package com.yatik.qrscanner.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@Database(
    version = 2,
    entities = [BarcodeData::class],
    autoMigrations = [
        AutoMigration (from = 1, to = 2)
    ]
)
abstract class BarcodeRoomDataBase : RoomDatabase() {
    abstract fun barcodeDao(): BarcodeDao

}