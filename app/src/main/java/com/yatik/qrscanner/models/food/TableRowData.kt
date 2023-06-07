package com.yatik.qrscanner.models.food

data class TableRowData(
    val nutritionType: String,
    val per100Value: String? = null,
    val perServing: String? = null
)
