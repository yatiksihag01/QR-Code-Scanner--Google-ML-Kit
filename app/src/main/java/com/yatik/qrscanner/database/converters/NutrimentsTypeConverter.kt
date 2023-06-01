package com.yatik.qrscanner.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.yatik.qrscanner.models.food.Nutriments

class NutrimentsTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromNutriments(nutriments: Nutriments?): String? {
        return gson.toJson(nutriments)
    }

    @TypeConverter
    fun toNutriments(json: String?): Nutriments? {
        if (json == null) return null
        return gson.fromJson(json, Nutriments::class.java)
    }
}