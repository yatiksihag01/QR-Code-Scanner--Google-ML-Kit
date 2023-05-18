package com.yatik.qrscanner.repository

import com.yatik.qrscanner.models.BarcodeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeBarcodeDataRepository : BarcodeDataRepository {

    private val _barcodeDataList = mutableListOf<BarcodeData>()
    private val barcodeDataList: List<BarcodeData>
        get() = _barcodeDataList

    override suspend fun insert(barcodeData: BarcodeData) {
        _barcodeDataList.add(barcodeData)
    }

    override suspend fun delete(barcodeData: BarcodeData) {
        _barcodeDataList.remove(barcodeData)
    }

    override fun getAllBarcodes(): Flow<List<BarcodeData>> {
        return flow {
            emit(barcodeDataList)
        }
    }

    override suspend fun deleteAll() {
        _barcodeDataList.clear()
    }

}