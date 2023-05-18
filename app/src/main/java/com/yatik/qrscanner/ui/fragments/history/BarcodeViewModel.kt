package com.yatik.qrscanner.ui.fragments.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.repository.BarcodeDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val repository: BarcodeDataRepository
) : ViewModel() {

    // As opposed to Flow, LiveData is lifecycle aware
    fun getAllBarcodes(): LiveData<List<BarcodeData>> =
        repository.getAllBarcodes().asLiveData()

    fun insert(barcodeData: BarcodeData) = viewModelScope.launch {
        repository.insert(barcodeData)
    }

    fun delete(barcodeData: BarcodeData) = viewModelScope.launch {
        repository.delete(barcodeData)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

}