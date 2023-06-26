package com.yatik.qrscanner.ui.fragments.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.repository.history.BarcodeDataRepository
import com.yatik.qrscanner.utils.Constants.Companion.ITEMS_PER_PAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class BarcodeViewModel @Inject constructor(
    private val repository: BarcodeDataRepository
) : ViewModel() {

    val pagingDataFlow = Pager(
        config = PagingConfig(pageSize = ITEMS_PER_PAGE)
    ) { repository.getAllBarcodes() }
        .flow
        .cachedIn(viewModelScope)

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