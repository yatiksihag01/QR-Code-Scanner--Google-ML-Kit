package com.yatik.qrscanner.di

import android.content.Context
import androidx.room.Room
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.database.BarcodeRoomDataBase
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
    ).build()

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
    fun provideIODispatcher(): CoroutineDispatcher =
        Dispatchers.IO

}