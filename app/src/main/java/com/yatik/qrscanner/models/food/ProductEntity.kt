package com.yatik.qrscanner.models.food

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yatik.qrscanner.database.converters.NutrimentsTypeConverter
import com.yatik.qrscanner.database.converters.NutriscoreTypeConverter

@Keep
@Entity(tableName = "food_product")
data class ProductEntity(

    @ColumnInfo(name = "nutriments")
    @TypeConverters(NutrimentsTypeConverter::class)
    val nutriments: Nutriments? = null,

    @ColumnInfo(name = "nutriscore_data")
    @TypeConverters(NutriscoreTypeConverter::class)
    val nutriscoreData: NutriscoreData? = null,

    @ColumnInfo(name = "nutrition_grade")
    val nutritionGrades: String? = null,

    @ColumnInfo(name = "product_name")
    val productName: String,

    @ColumnInfo(name = "image_front_small_url")
    val frontImageSmall: String? = null,

    @ColumnInfo(name = "brands")
    val brands: String,

    @ColumnInfo(name = "quantity")
    val quantity: String,

    @ColumnInfo("id")
    @PrimaryKey(autoGenerate = false)
    val barcode: String,

    @ColumnInfo("timestamp")
    val timestamp: Long
)