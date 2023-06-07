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
        productName = productEntity.productName,
        frontImageSmall = productEntity.frontImageSmall,
        brands = productEntity.brands,
        quantity = productEntity.quantity
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
        productName = product.productName ?: "Product name not found",
        frontImageSmall = product.frontImageSmall,
        brands = product.brands ?: "Brand name unavailable",
        quantity = product.quantity ?: "unknown quantity",
        barcode = barcode,
        timestamp = timestamp
    )
}