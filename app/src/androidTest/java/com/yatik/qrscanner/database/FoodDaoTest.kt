package com.yatik.qrscanner.database

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.yatik.qrscanner.models.food.Nutriments
import com.yatik.qrscanner.models.food.NutriscoreData
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.utils.mappers.productToProductEntity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@OptIn(ExperimentalCoroutinesApi::class)
@SmallTest
@HiltAndroidTest
class FoodDaoTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var dataBase: BarcodeRoomDataBase
    private lateinit var dao: FoodDao

    @Before
    fun setup() {
        hiltRule.inject()
        dao = dataBase.foodDao()
    }

    @After
    fun tearDown() {
        dataBase.close()
    }

    @Test
    fun getProductTest() = runTest {

        val product = Product(
            nutriments = Nutriments(),
            nutriscoreData = NutriscoreData(),
            nutritionGrades = "e",
            productName = "sample_product",
            frontImageSmall = "https://openfoodfacts.org/sample_image",
            brands = "Sample brand",
            quantity = "250 grams"
        )

        val barcode = "123456789123"
        val productEntity = productToProductEntity(product, barcode, 1622515271000)

        dao.upsertProduct(productEntity)
        val dbProductEntity = dao.getProduct(barcode)

        assertThat(dbProductEntity).isNotNull()
        assertThat(dbProductEntity).isEqualTo(productEntity)
        assertThat(dao.getProduct("789")).isNull()
    }

}