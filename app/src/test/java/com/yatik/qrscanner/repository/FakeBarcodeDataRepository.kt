package com.yatik.qrscanner.repository

import androidx.paging.PagingSource
import com.yatik.qrscanner.FakePagingSource
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.repository.history.BarcodeDataRepository

/*
 * Copyright 2023 Yatik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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