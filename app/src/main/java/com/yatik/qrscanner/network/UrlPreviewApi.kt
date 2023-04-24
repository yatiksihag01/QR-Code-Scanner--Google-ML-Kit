package com.yatik.qrscanner.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface UrlPreviewApi {

    @GET
    suspend fun getUrlPreview(@Url url: String): Response<ResponseBody>

}