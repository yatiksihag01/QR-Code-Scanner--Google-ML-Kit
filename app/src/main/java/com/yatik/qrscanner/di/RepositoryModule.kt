package com.yatik.qrscanner.di

import com.yatik.qrscanner.repository.BarcodeDataRepository
import com.yatik.qrscanner.repository.DefaultBarcodeDataRepository
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
    ) : BarcodeDataRepository

}