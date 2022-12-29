package com.yatik.qrscanner.others

import android.app.Application
import com.yatik.qrscanner.database.BarcodeRoomDataBase
import com.yatik.qrscanner.repository.BarcodeDataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class BarcodeDataApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())
    val database by lazy { BarcodeRoomDataBase.getDatabase(this, applicationScope) }
    val repository by lazy { BarcodeDataRepository(database.barcodeDao()) }
}