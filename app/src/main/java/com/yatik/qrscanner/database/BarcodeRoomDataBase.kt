package com.yatik.qrscanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [BarcodeData::class], version = 1, exportSchema = false)
abstract class BarcodeRoomDataBase : RoomDatabase() {

    abstract fun barcodeDao(): BarcodeDao

    companion object {

        @Volatile
        private var INSTANCE: BarcodeRoomDataBase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BarcodeRoomDataBase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BarcodeRoomDataBase::class.java,
                    "barcode_database")
                .addCallback(BarcodeDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        private class BarcodeDatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(database.barcodeDao())
                    }
                }
            }

            suspend fun populateDatabase(barcodeDao: BarcodeDao) {
                barcodeDao.deleteAll()

                // Add sample data.
                val detail = BarcodeData(Barcode.TYPE_TEXT, "Sample Text", null)
                barcodeDao.insert(detail)
            }
        }
    }


}