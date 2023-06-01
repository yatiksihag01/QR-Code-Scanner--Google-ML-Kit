package com.yatik.qrscanner.repository.details

import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DetailsRepository {

    suspend fun getUrlInfo(url: String): Flow<Resource<UrlPreviewData>>
    suspend fun getFoodDetails(barcode: String): Flow<Resource<Product>>

}