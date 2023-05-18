package com.yatik.qrscanner.ui.fragments.details

import com.yatik.qrscanner.database.UrlPreviewDao
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.network.UrlPreviewApi
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.connectivity.ConnectivityHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DefaultDetailsRepository @Inject constructor(
    private val api: UrlPreviewApi,
    private val urlPreviewDao: UrlPreviewDao,
    private val connectivityHelper: ConnectivityHelper
) : DetailsRepository {

    override fun getUrlInfo(
        url: String
    ): Flow<Resource<UrlPreviewData>> = flow {
        emit(Resource.Loading())
        val urlPreviewData = urlPreviewDao.getUrlInfo(url)
        emit(
            Resource.Loading(
                data = urlPreviewData
            )
        )
        if (connectivityHelper.isConnectedToInternet()) {
            fetchUrlPreview(url, urlPreviewData).collect { resource ->
                emit(resource)
            }
        } else emit(
            Resource.Error(
                data = urlPreviewData,
                message = "No internet connection"
            )
        )
    }

    private suspend fun fetchUrlPreview(
        url: String,
        urlPreviewData: UrlPreviewData?
    ): Flow<Resource<UrlPreviewData>> = flow {
        try {
            val response = api.getUrlPreview(url)
            if (response.isSuccessful) {
                val htmlResponse = response.body()?.string() ?: ""
                val data = parseHtml(url, htmlResponse)
                if (urlPreviewData != null)
                    urlPreviewDao.deleteUrlInfo(urlPreviewData)
                urlPreviewDao.upsertUrlInfo(data)
                emit(
                    Resource.Success(
                        data = urlPreviewDao.getUrlInfo(url)!!
                    )
                )
            }
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    message = "Unexpected HTTP response",
                    data = urlPreviewData
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    message = "Couldn't reach server",
                    data = urlPreviewData
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    message = "OOPS! Something went wrong",
                    data = urlPreviewData
                )
            )
        }
    }

    private fun parseHtml(mainUrl: String, htmlResponse: String): UrlPreviewData {
        val doc = Jsoup.parse(htmlResponse)
        val title = doc.title()
        val description = doc.select("meta[property=og:description]").attr("content")
        val imageUrl = doc.select("meta[property=og:image]").attr("content")
        return UrlPreviewData(mainUrl, title, description, imageUrl)
    }

}