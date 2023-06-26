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