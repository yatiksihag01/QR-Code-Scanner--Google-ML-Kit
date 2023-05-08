package com.yatik.qrscanner.repository

import androidx.paging.PagingSource
import com.yatik.qrscanner.FakePagingSource
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.repository.history.BarcodeDataRepository

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

    override fun getAllBarcodes(): PagingSource<Int, BarcodeData> {
        return FakePagingSource(barcodeDataList)
    }

    override suspend fun deleteAll() {
        _barcodeDataList.clear()
    }

}