package com.yatik.qrscanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(barcodeData: BarcodeData)

    @Delete
    suspend fun delete(barcodeData: BarcodeData)

    @Query("SELECT * FROM barcode_table ORDER BY id DESC")
    fun getAllBarcodes(): Flow<List<BarcodeData>>

    @Query("DELETE FROM barcode_table")
    suspend fun deleteAll()
}