package com.yatik.qrscanner.utils

import android.content.Context
import android.widget.TextView
import com.google.android.material.color.MaterialColors.getColor
import com.yatik.qrscanner.R
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.models.food.TableRowData

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

fun foodTableRowsList(product: Product): List<TableRowData> {

    if (product.nutriments == null)
        return listOf(
            TableRowData("No data found...")
        )

    val nutriments = product.nutriments
    val unit =
        if (product.nutriscoreData?.isBeverage == 1 || product.nutriscoreData?.isWater == 1) "mL"
        else "grams"

    val energyKcal100g = setValOrDash(nutriments.energyKcal100g.toString(), "kcal")
    val energyServing = setValOrDash(nutriments.energyKcalServing.toString(), "kcal")

    val carb100g = setValOrDash(nutriments.carbohydrates100g.toString(), unit)
    val carbServing = setValOrDash(nutriments.carbohydratesServing.toString(), unit)

    val fat100g = setValOrDash(nutriments.fat100g.toString(), unit)
    val fatServing = setValOrDash(nutriments.fatServing.toString(), unit)

    val sugar100g = setValOrDash(nutriments.sugars100g.toString(), unit)
    val sugarServing = setValOrDash(nutriments.sugarServing.toString(), unit)

    val fiber100g = setValOrDash(nutriments.fiber100g.toString(), unit)
    val fiberServing = setValOrDash(nutriments.fiberServing.toString(), unit)

    val proteins100g = setValOrDash(nutriments.proteins100g.toString(), unit)
    val proteinsServing = setValOrDash(nutriments.proteinsServing.toString(), unit)

    val salt100g = setValOrDash(nutriments.salt100g.toString(), unit)
    val saltServing = setValOrDash(nutriments.saltServing.toString(), unit)

    val sodium100g = setValOrDash(nutriments.sodium100g.toString(), unit)
    val sodiumServing = setValOrDash(nutriments.sodiumServing.toString(), unit)

    return listOf(
        TableRowData("Energy", energyKcal100g, energyServing),
        TableRowData("Carbohydrates", carb100g, carbServing),
        TableRowData("Fat", fat100g, fatServing),
        TableRowData("Sugars", sugar100g, sugarServing),
        TableRowData("Fiber", fiber100g, fiberServing),
        TableRowData("Proteins", proteins100g, proteinsServing),
        TableRowData("Salt", salt100g, saltServing),
        TableRowData("Sodium", sodium100g, sodiumServing)
    )
}

/**
 * Returns "--" if the given value is [isNullOrBlank], "null", or "-1".
 *
 * Otherwise, returns the "value" followed by the "unit", for example, "520 kcal".
 */
private fun setValOrDash(value: String?, unit: String): String =
    if (value.isNullOrBlank() || value == "null" || value == "-1") "   --   "
    else "$value $unit"

fun getNovaInfo(context: Context, value: Int?): String {
    return when (value) {
        1 -> context.getString(R.string.nova_1_info)
        2 -> context.getString(R.string.nova_2_info)
        3 -> context.getString(R.string.nova_3_info)
        4 -> context.getString(R.string.nova_4_info)
        else -> context.getString(R.string.nova_unknown_info)
    }
}

fun setNovaColor(context: Context, textView: TextView, value: Int?) {
    textView.text = value?.toString() ?: "?"
    when (value) {
        1 -> textView.setBackgroundColor(context.getColor(R.color.dark_green))
        2 -> textView.setBackgroundColor(context.getColor(R.color.yellow))
        3 -> textView.setBackgroundColor(context.getColor(R.color.orange))
        4 -> textView.setBackgroundColor(context.getColor(R.color.redButton))
        else -> textView.setBackgroundColor(getColor(textView, R.attr.semiTransparent))
    }
}

fun getNutriInfo(context: Context, value: String?): String {
    return when (value) {
        "a" -> context.getString(R.string.nutri_score_a)
        "b" -> context.getString(R.string.nutri_score_b)
        "c" -> context.getString(R.string.nutri_score_c)
        "d" -> context.getString(R.string.nutri_score_d)
        "e" -> context.getString(R.string.nutri_score_e)
        else -> context.getString(R.string.nutri_score_unknown)
    }
}

fun setNutriColor(context: Context, textView: TextView, value: String?) {
    textView.text = value ?: "?"
    when (value) {
        "a" -> textView.setBackgroundColor(context.getColor(R.color.dark_green))
        "b" -> textView.setBackgroundColor(context.getColor(R.color.light_green))
        "c" -> textView.setBackgroundColor(context.getColor(R.color.yellow))
        "d" -> textView.setBackgroundColor(context.getColor(R.color.orange))
        "e" -> textView.setBackgroundColor(context.getColor(R.color.redButton))
        else -> textView.setBackgroundColor(getColor(textView, R.attr.semiTransparent))
    }
}

fun isBookBarcode(barcode: String): Boolean =
    barcode.startsWith("978")

fun handleMalformedNutrimentsJson(malformedJson: String): String =
    malformedJson.replace("unit\":,", "unit\":\"   --   \",")
        .replace(":,", ":-1,")
