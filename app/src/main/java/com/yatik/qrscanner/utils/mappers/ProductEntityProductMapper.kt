package com.yatik.qrscanner.utils.mappers

import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.models.food.ProductEntity

fun productEntityToProduct(
    productEntity: ProductEntity
): Product {
    return Product(
        nutriments = productEntity.nutriments,
        nutriscoreData = productEntity.nutriscoreData,
        nutritionGrades = productEntity.nutritionGrades,
        productName = productEntity.productName
    )
}

fun productToProductEntity(
    product: Product,
    barcode: String,
    timestamp: Long
): ProductEntity {
    return ProductEntity(
        nutriments = product.nutriments,
        nutriscoreData = product.nutriscoreData,
        nutritionGrades = product.nutritionGrades,
        productName = product.productName,
        barcode = barcode,
        timestamp = timestamp
    )
}