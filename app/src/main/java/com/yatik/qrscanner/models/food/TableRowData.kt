package com.yatik.qrscanner.models.food

import androidx.annotation.Keep

@Keep
data class TableRowData(
    val nutritionType: String,
    val per100Value: String? = null,
    val perServing: String? = null
)
