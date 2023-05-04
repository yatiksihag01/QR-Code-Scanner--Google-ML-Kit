package com.yatik.qrscanner.repository.details

import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DetailsRepository {

    suspend fun getUrlInfo(url: String): Flow<Resource<UrlPreviewData>>

}