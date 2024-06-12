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

package com.yatik.qrscanner.ui.fragments.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.repository.history.BarcodeDataRepository
import com.yatik.qrscanner.utils.Constants.Companion.ITEMS_PER_PAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val repository: BarcodeDataRepository
) : ViewModel() {

    private val mSearchQuery = MutableStateFlow("")
    private val searchedPagingDataFlow =
        MutableStateFlow<PagingData<BarcodeDetails>>(PagingData.empty())
    private val pagingDataFlow = repository.getPagingDataStream(ITEMS_PER_PAGE)
        .cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val combinedFlow: Flow<PagingData<BarcodeDetails>> = mSearchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            pagingDataFlow
        } else {
            searchedPagingDataFlow
        }
    }

    fun insert(barcodeDetails: BarcodeDetails) = viewModelScope.launch {
        repository.insert(barcodeDetails)
    }

    fun undoDeletion(barcodeDetails: BarcodeDetails) = viewModelScope.launch {
        repository.undoDeletion(barcodeDetails)
    }

    fun delete(barcodeDetails: BarcodeDetails) = viewModelScope.launch {
        repository.delete(barcodeDetails)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun searchFromBarcodes(searchQuery: String) = viewModelScope.launch {
        mSearchQuery.value = searchQuery
        if (searchQuery.isBlank()) return@launch
        repository.getSearchedDataStream(searchQuery, ITEMS_PER_PAGE)
            .cachedIn(viewModelScope)
            .collectLatest {
                searchedPagingDataFlow.value = it
            }
    }

}