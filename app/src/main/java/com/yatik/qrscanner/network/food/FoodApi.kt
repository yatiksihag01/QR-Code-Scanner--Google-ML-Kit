package com.yatik.qrscanner.network.food

import com.yatik.qrscanner.models.food.FoodResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

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

interface FoodApi {

    @GET("api/v2/product/{barcode}")
    suspend fun getFoodDetails(
        @Path("barcode")
        barcode: String,
        @Query("fields")
        fields: String = "product_name,nutriscore_data,nutriments,nutrition_grades,image_front_small_url,brands,quantity"
    ): Response<FoodResponse>

}