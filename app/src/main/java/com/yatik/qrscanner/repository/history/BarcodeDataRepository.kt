package com.yatik.qrscanner.repository.history

import androidx.paging.PagingSource
import com.yatik.qrscanner.models.BarcodeData

interface BarcodeDataRepository {

    suspend fun insert(barcodeData: BarcodeData)
    suspend fun delete(barcodeData: BarcodeData)
    fun getAllBarcodes(): PagingSource<Int, BarcodeData>
    suspend fun deleteAll()

}