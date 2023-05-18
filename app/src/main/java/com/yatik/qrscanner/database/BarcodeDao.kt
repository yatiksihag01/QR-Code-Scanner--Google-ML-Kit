package com.yatik.qrscanner.database

import androidx.paging.PagingSource
import androidx.room.*
import com.yatik.qrscanner.models.BarcodeData

@Dao
interface BarcodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(barcodeData: BarcodeData)

    @Delete
    suspend fun delete(barcodeData: BarcodeData)

    @Query("SELECT * FROM barcode_table ORDER BY id DESC")
    fun getAllBarcodes(): PagingSource<Int, BarcodeData>

    @Query("DELETE FROM barcode_table")
    suspend fun deleteAll()

}