package com.yatik.qrscanner

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yatik.qrscanner.models.BarcodeData

class FakePagingSource(private val barcodeDataList: List<BarcodeData>) :
    PagingSource<Int, BarcodeData>() {

    override fun getRefreshKey(state: PagingState<Int, BarcodeData>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BarcodeData> {
        val data = barcodeDataList
        val prevKey: Int? = null
        val nextKey: Int? = null
        return LoadResult.Page(data, prevKey, nextKey)
    }

}
