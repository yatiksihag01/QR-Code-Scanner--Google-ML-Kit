package com.yatik.qrscanner.repository.history

import androidx.paging.PagingSource
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultBarcodeDataRepository @Inject constructor(
    private val barcodeDao: BarcodeDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BarcodeDataRepository {

    override suspend fun insert(barcodeData: BarcodeData) = withContext(ioDispatcher) {
        barcodeDao.insert(barcodeData)
    }

    override suspend fun delete(barcodeData: BarcodeData) = withContext(ioDispatcher) {
        barcodeDao.delete(barcodeData)
    }

    override fun getAllBarcodes(): PagingSource<Int, BarcodeData> {
        return barcodeDao.getAllBarcodes()
    }

    override suspend fun deleteAll() = withContext(ioDispatcher) {
        barcodeDao.deleteAll()
    }

}