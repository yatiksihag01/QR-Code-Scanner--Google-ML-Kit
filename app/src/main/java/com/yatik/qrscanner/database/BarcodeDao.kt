package com.yatik.qrscanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BarcodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(barcodeData: BarcodeData)

    @Delete
    fun delete(barcodeData: BarcodeData)

    @Query("SELECT * FROM barcode_table ORDER BY id DESC")
    fun getAllBarcodes(): kotlinx.coroutines.flow.Flow<List<BarcodeData>>

    @Query("DELETE FROM barcode_table")
    suspend fun deleteAll()
}