package com.yatik.qrscanner.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yatik.qrscanner.models.food.Nutriments
import com.yatik.qrscanner.utils.handleMalformedNutrimentsJson

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