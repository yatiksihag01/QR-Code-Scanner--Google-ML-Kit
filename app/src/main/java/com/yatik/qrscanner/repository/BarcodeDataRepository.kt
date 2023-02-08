package com.yatik.qrscanner.repository

import androidx.annotation.WorkerThread
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.flow.Flow

class BarcodeDataRepository(private val barcodeDao: BarcodeDao) {

    val listFlow: Flow<List<BarcodeData>> = barcodeDao.getAllBarcodes()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(barcodeData: BarcodeData) {
        barcodeDao.insert(barcodeData)
    }

    @WorkerThread
    suspend fun delete(barcodeData: BarcodeData) {
        barcodeDao.delete(barcodeData)
    }

    @WorkerThread
    suspend fun deleteAll() {
        barcodeDao.deleteAll()
    }

}