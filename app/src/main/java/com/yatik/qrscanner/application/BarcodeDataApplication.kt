package com.yatik.qrscanner.application

import android.app.Application
import com.yatik.qrscanner.database.BarcodeRoomDataBase
import com.yatik.qrscanner.repository.BarcodeDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BarcodeDataApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { BarcodeRoomDataBase.getDatabase(this, applicationScope) }
    val repository by lazy { BarcodeDataRepository(database.barcodeDao()) }
}