package com.yatik.qrscanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yatik.qrscanner.models.food.ProductEntity

@Dao
interface FoodDao {

    @Upsert
    suspend fun upsertProduct(productEntity: ProductEntity)

    @Delete
    suspend fun deleteProduct(productEntity: ProductEntity)

    @Query("SELECT * FROM food_product WHERE id = :barcode")
    suspend fun getProduct(barcode: String): ProductEntity?
}