package com.yatik.qrscanner.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.yatik.qrscanner.models.food.NutriscoreData

class NutriscoreTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromNutriscoreData(nutriscoreData: NutriscoreData?): String? {
        return gson.toJson(nutriscoreData)
    }

    @TypeConverter
    fun toNutriscoreData(json: String?): NutriscoreData? {
        if (json == null) return null
        return gson.fromJson(json, NutriscoreData::class.java)
    }
}