package com.yatik.qrscanner.repository.history

import androidx.annotation.WorkerThread
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultBarcodeDataRepository @Inject constructor(
    private val barcodeDao: BarcodeDao
) : BarcodeDataRepository {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(barcodeData: BarcodeData) {
        barcodeDao.insert(barcodeData)
    }

    @WorkerThread
    override suspend fun delete(barcodeData: BarcodeData) {
        barcodeDao.delete(barcodeData)
    }

    @WorkerThread
    override fun getAllBarcodes(): Flow<List<BarcodeData>> {
        return barcodeDao.getAllBarcodes()
    }

    @WorkerThread
    override suspend fun deleteAll() {
        barcodeDao.deleteAll()
    }

}