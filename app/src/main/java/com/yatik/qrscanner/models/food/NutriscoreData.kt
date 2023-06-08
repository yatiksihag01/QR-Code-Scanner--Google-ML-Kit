package com.yatik.qrscanner.models.food

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NutriscoreData(

    // Energy
    val energy: Number? = null,
    @SerializedName("energy_points")
    val energyPoints: Int? = null,
    @SerializedName("energy_value")
    val energyValue: Number? = null,

    // Fiber
    val fiber: Number? = null,
    @SerializedName("fiber_points")
    val fiberPoints: Int? = null,
    @SerializedName("fiber_value")
    val fiberValue: Number? = null,

    val grade: String? = null,
    @SerializedName("is_beverage")
    val isBeverage: Int? = null,
    @SerializedName("is_cheese")
    val isCheese: Int? = null,
    @SerializedName("is_fat")
    val isFat: Int? = null,
    @SerializedName("is_water")
    val isWater: Int? = null,

    @SerializedName("negative_points")
    val negativePoints: Int? = null,
    @SerializedName("positive_points")
    val positivePoints: Int? = null,

    // Protein
    val proteins: Number? = null,
    @SerializedName("proteins_points")
    val proteinsPoints: Int? = null,
    @SerializedName("proteins_value")
    val proteinsValue: Number? = null,
    @SerializedName("saturated_fat")

    // Saturated Fat
    val saturatedFat: Number? = null,
    @SerializedName("saturated_fat_points")
    val saturatedFatPoints: Int? = null,
    @SerializedName("saturated_fat_ratio")
    val saturatedFatRatio: Double? = null,
    @SerializedName("saturated_fat_ratio_points")
    val saturatedFatRatioPoints: Int? = null,
    @SerializedName("saturated_fat_ratio_value")
    val saturatedFatRatioValue: Double? = null,
    @SerializedName("saturated_fat_value")
    val saturatedFatValue: Number? = null,

    // Sodium
    val score: Number? = null,
    val sodium: Number? = null,
    @SerializedName("sodium_points")
    val sodiumPoints: Int? = null,
    @SerializedName("sodium_value")
    val sodiumValue: Number? = null,

    // Sugar
    val sugars: Number? = null,
    @SerializedName("sugars_points")
    val sugarsPoints: Int? = null,
    @SerializedName("sugars_value")
    val sugarsValue: Number? = null
)