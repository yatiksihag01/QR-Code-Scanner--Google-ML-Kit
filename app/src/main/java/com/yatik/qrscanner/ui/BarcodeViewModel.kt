package com.yatik.qrscanner.ui

import androidx.lifecycle.*
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.repository.BarcodeDataRepository
import kotlinx.coroutines.launch

class BarcodeViewModel(private val repository: BarcodeDataRepository) : ViewModel() {

    // As opposed to Flow, LiveData is lifecycle aware
    val allBarcodes: LiveData<List<BarcodeData>> = repository.listFlow.asLiveData()

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

class BarcodeViewModelFactory(private val repository: BarcodeDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BarcodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BarcodeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}