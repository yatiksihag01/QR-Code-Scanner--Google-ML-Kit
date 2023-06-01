package com.yatik.qrscanner.models.food

import com.google.gson.annotations.SerializedName

data class FoodResponse(
    val code: String,
    val product: Product?,
    val status: Int,
    @SerializedName("status_verbose")
    val statusVerbose: String
)