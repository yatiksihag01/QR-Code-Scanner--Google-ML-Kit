package com.yatik.qrscanner.utils.mappers

import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.models.food.ProductEntity

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