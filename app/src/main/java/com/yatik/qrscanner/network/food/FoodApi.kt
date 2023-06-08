package com.yatik.qrscanner.network.food

import com.yatik.qrscanner.models.food.FoodResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {

    @GET("api/v2/product/{barcode}")
    suspend fun getFoodDetails(
        @Path("barcode")
        barcode: String,
        @Query("fields")
        fields: String = "product_name,nutriscore_data,nutriments,nutrition_grades,image_front_small_url,brands,quantity"
    ): Response<FoodResponse>

}