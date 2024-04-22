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

package com.yatik.qrscanner.repository.details

import com.yatik.qrscanner.database.FoodDao
import com.yatik.qrscanner.database.UrlPreviewDao
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.food.FoodResponse
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.network.UrlPreviewApi
import com.yatik.qrscanner.network.food.FoodApi
import com.yatik.qrscanner.utils.Constants.Companion.PRODUCT_CACHE_TIME
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.connectivity.ConnectivityHelper
import com.yatik.qrscanner.utils.mappers.productEntityToProduct
import com.yatik.qrscanner.utils.mappers.productToProductEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DefaultDetailsRepository @Inject constructor(
    private val urlPreviewApi: UrlPreviewApi,
    private val urlPreviewDao: UrlPreviewDao,
    private val foodDao: FoodDao,
    private val foodApi: FoodApi,
    private val connectivityHelper: ConnectivityHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DetailsRepository {

    override suspend fun getUrlInfo(
        url: String
    ): Flow<Resource<UrlPreviewData>> = withContext(ioDispatcher) {
        flow {
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
    }

    private suspend fun fetchUrlPreview(
        url: String,
        urlPreviewData: UrlPreviewData?
    ): Flow<Resource<UrlPreviewData>> = flow {
        try {
            val response = urlPreviewApi.getUrlPreview(url)
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
            } else emit(Resource.Error(message = "Error response"))
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

    override suspend fun getFoodDetails(barcode: String): Flow<Resource<Product>> =
        withContext(ioDispatcher) {
            flow {
                emit(Resource.Loading())
                val currentTime = System.currentTimeMillis()
                var timestamp = currentTime - (PRODUCT_CACHE_TIME + 60 * 60 * 1000)
                val dbProduct = foodDao.getProduct(barcode)
                val product: Product? = dbProduct?.let { productEntityToProduct(it) }

                if (dbProduct != null) {
                    emit(Resource.Loading(product))
                    timestamp = dbProduct.timestamp
                }

                val elapsedTime = currentTime - timestamp

                if (connectivityHelper.isConnectedToInternet() && elapsedTime > PRODUCT_CACHE_TIME) {
                    fetchFoodDetails(barcode).collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val fetchedProduct = resource.data?.product
                                if (resource.data?.status == 1 && fetchedProduct != null) {
                                    foodDao.upsertProduct(
                                        productToProductEntity(
                                            fetchedProduct, barcode, System.currentTimeMillis()
                                        )
                                    )
                                    emit(Resource.Success(data = fetchedProduct))
                                } else {
                                    emit(Resource.Error(message = "Product not found"))
                                }
                            }

                            else -> emit(Resource.Error(message = resource.message!!))
                        }
                    }
                } else if (elapsedTime <= PRODUCT_CACHE_TIME) {
                    emit(Resource.Success(data = product!!))
                } else {
                    emit(Resource.Error(message = "No internet connection", data = product))
                }
            }
        }

    private suspend fun fetchFoodDetails(
        barcode: String
    ): Flow<Resource<FoodResponse>> = flow {
        try {
            val foodResponse = foodApi.getFoodDetails(barcode)
            if (foodResponse.isSuccessful) {
                foodResponse.body()?.let {
                    emit(Resource.Success(data = it))
                }
            } else emit(Resource.Error("Product not found"))
        } catch (e: HttpException) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    message = "Unexpected HTTP response"
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    message = "Couldn't reach server"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emit(
                Resource.Error(
                    message = "OOPS! Something went wrong"
                )
            )
        }
    }

}