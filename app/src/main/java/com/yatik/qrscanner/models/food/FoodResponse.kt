package com.yatik.qrscanner.models.food

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FoodResponse(
    val code: String,
    val product: Product?,
    val status: Int,
    @SerializedName("status_verbose")
    val statusVerbose: String
)