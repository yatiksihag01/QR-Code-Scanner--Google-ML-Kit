package com.yatik.qrscanner.ui.fragments.details

import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DetailsRepository {

    fun getUrlInfo(url: String): Flow<Resource<UrlPreviewData>>

}