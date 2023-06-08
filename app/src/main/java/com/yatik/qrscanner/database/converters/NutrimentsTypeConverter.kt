package com.yatik.qrscanner.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yatik.qrscanner.models.food.Nutriments
import com.yatik.qrscanner.utils.handleMalformedNutrimentsJson

class NutrimentsTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromNutriments(nutriments: Nutriments?): String? {
        return gson.toJson(nutriments)
    }

    @TypeConverter
    fun toNutriments(json: String?): Nutriments? {
        if (json == null) return null
        return try {
            gson.fromJson(json, Nutriments::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            val correctedJson = handleMalformedNutrimentsJson(json)
            if (correctedJson == json) null
            else gson.fromJson(
                handleMalformedNutrimentsJson(json),
                Nutriments::class.java
            )
        }
    }

}