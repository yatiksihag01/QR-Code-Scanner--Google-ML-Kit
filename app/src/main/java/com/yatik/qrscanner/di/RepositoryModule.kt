package com.yatik.qrscanner.di

import com.yatik.qrscanner.repository.barcode_generator.DefaultGeneratorRepository
import com.yatik.qrscanner.repository.barcode_generator.GeneratorRepository
import com.yatik.qrscanner.repository.details.DefaultDetailsRepository
import com.yatik.qrscanner.repository.details.DetailsRepository
import com.yatik.qrscanner.repository.history.BarcodeDataRepository
import com.yatik.qrscanner.repository.history.DefaultBarcodeDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBarcodeDataRepository(
        defaultBarcodeDataRepository: DefaultBarcodeDataRepository
    ): BarcodeDataRepository

    @Binds
    @Singleton
    abstract fun bindGeneratorRepository(
        defaultGeneratorRepository: DefaultGeneratorRepository
    ): GeneratorRepository

    @Binds
    @Singleton
    abstract fun bindUrlPreviewRepository(
        defaultDetailsRepository: DefaultDetailsRepository
    ): DetailsRepository

}