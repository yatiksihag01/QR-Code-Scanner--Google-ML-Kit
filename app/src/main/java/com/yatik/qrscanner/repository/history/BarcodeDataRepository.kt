package com.yatik.qrscanner.repository.history

import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.flow.Flow

interface BarcodeDataRepository {

    suspend fun insert(barcodeData: BarcodeData)
    suspend fun delete(barcodeData: BarcodeData)
    fun getAllBarcodes(): Flow<List<BarcodeData>>
    suspend fun deleteAll()

}