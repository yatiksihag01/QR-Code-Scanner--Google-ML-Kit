package com.yatik.qrscanner.models.food

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Product(
    val nutriments: Nutriments? = null,
    @SerializedName("nutriscore_data")
    val nutriscoreData: NutriscoreData? = null,
    @SerializedName("nutrition_grades")
    val nutritionGrades: String? = null,
    @SerializedName("product_name")
    val productName: String? = null
)