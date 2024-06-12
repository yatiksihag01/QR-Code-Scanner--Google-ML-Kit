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

package com.yatik.qrscanner.di

import android.content.Context
import androidx.room.Room
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.database.BarcodeRoomDataBase
import com.yatik.qrscanner.database.FoodDao
import com.yatik.qrscanner.database.UrlPreviewDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBarcodeDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext,
        BarcodeRoomDataBase::class.java,
        "barcode_database"
    ).addMigrations(BarcodeRoomDataBase.MIGRATION_4_5)
        .build()

    @Provides
    @Singleton
    fun provideBarcodeDao(database: BarcodeRoomDataBase): BarcodeDao {
        return database.barcodeDao()
    }

    @Provides
    @Singleton
    fun provideUrlPreviewDao(
        database: BarcodeRoomDataBase
    ): UrlPreviewDao = database.urlPreviewDao()

    @Provides
    @Singleton
    fun provideFoodDao(
        database: BarcodeRoomDataBase
    ): FoodDao = database.foodDao()

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher =
        Dispatchers.IO

}