package com.yatik.qrscanner.models.food

import com.google.gson.annotations.SerializedName

data class Nutriments(

    // Carbohydrate
    val carbohydrates: Number? = null,
    @SerializedName("carbohydrates_100g")
    val carbohydrates100g: Number? = null,
    @SerializedName("carbohydrates_unit")
    val carbohydratesUnit: String? = null,
    @SerializedName("carbohydrates_value")
    val carbohydratesValue: Number? = null,

    // Energy
    val energy: Number? = null,
    @SerializedName("energy-kcal")
    val energyKcal: Number? = null,
    @SerializedName("energy-kcal_100g")
    val energyKcal100g: Number? = null,
    @SerializedName("energy-kcal_unit")
    val energyKcalUnit: String? = null,
    @SerializedName("energy-kcal_value")
    val energyKcalValue: Number? = null,
    @SerializedName("energy-kcal_value_computed")
    val energyKcalValueComputed: Number? = null,
    @SerializedName("energy_100g")
    val energy100g: Number? = null,
    @SerializedName("energy_unit")
    val energyUnit: String? = null,
    @SerializedName("energy_value")
    val energyValue: Number? = null,

    // Fat
    val fat: Number? = null,
    @SerializedName("fat_100g")
    val fat100g: Number? = null,
    @SerializedName("fat_unit")
    val fatUnit: String? = null,
    @SerializedName("fat_value")
    val fatValue: Number? = null,

    // Nova Group
    @SerializedName("nova-group")
    val novaGroup: Int? = null,
    @SerializedName("nova-group_100g")
    val novaGroup100g: Int? = null,
    @SerializedName("nova-group_serving")
    val novaGroupServing: Int? = null,

    // Nutrition Score
    @SerializedName("nutrition-score-fr")
    val nutritionScoreFr: Number? = null,
    @SerializedName("nutrition-score-fr_100g")
    val nutritionScoreFr100g: Number? = null,

    // Proteins
    val proteins: Number? = null,
    @SerializedName("proteins_100g")
    val proteins100g: Number? = null,
    @SerializedName("proteins_unit")
    val proteinsUnit: String? = null,
    @SerializedName("proteins_value")
    val proteinsValue: Number? = null,

    // Salt
    val salt: Number? = null,
    @SerializedName("salt_100g")
    val salt100g: Number? = null,
    @SerializedName("salt_unit")
    val saltUnit: String? = null,
    @SerializedName("salt_value")
    val saltValue: Number? = null,

    // Saturated Fat
    @SerializedName("saturated-fat")
    val saturatedFat: Number? = null,
    @SerializedName("saturated-fat_100g")
    val saturatedFat100g: Number? = null,
    @SerializedName("saturated-fat_unit")
    val saturatedFatUnit: String? = null,
    @SerializedName("saturated-fat_value")
    val saturatedFatValue: Number? = null,

    // Sodium
    val sodium: Number? = null,
    @SerializedName("sodium_100g")
    val sodium100g: Number? = null,
    @SerializedName("sodium_unit")
    val sodiumUnit: String? = null,
    @SerializedName("sodium_value")
    val sodiumValue: Number? = null,

    // Sugar
    val sugars: Number? = null,
    @SerializedName("sugars_100g")
    val sugars100g: Number? = null,
    @SerializedName("sugars_unit")
    val sugarsUnit: String? = null,
    @SerializedName("sugars_value")
    val sugarsValue: Number? = null
)