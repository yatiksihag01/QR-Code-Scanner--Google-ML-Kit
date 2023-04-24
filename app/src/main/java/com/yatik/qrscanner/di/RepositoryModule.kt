package com.yatik.qrscanner.di

import com.yatik.qrscanner.repository.BarcodeDataRepository
import com.yatik.qrscanner.repository.DefaultBarcodeDataRepository
import com.yatik.qrscanner.repository.DefaultGeneratorRepository
import com.yatik.qrscanner.repository.GeneratorRepository
import com.yatik.qrscanner.ui.fragments.details.DefaultDetailsRepository
import com.yatik.qrscanner.ui.fragments.details.DetailsRepository
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