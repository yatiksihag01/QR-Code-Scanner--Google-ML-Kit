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

package com.yatik.qrscanner.ui.fragments.details

import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.repository.details.DetailsRepository
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.connectivity.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: DetailsRepository,
    private val connectivityHelper: ConnectivityHelper
) : ViewModel() {

    private val _urlPreviewResource: MutableLiveData<Resource<UrlPreviewData>> = MutableLiveData()
    val urlPreviewResource: LiveData<Resource<UrlPreviewData>>
        get() = _urlPreviewResource

    private val _foodProductResource: MutableLiveData<Resource<Product>> = MutableLiveData()
    val foodProductResource: LiveData<Resource<Product>>
        get() = _foodProductResource

    fun getUrlPreview(url: String) = viewModelScope.launch {
        repository.getUrlInfo(url).collect { resource ->
            _urlPreviewResource.postValue(resource)
        }
    }

    fun getFoodDetails(barcode: String) = viewModelScope.launch {
        repository.getFoodDetails(barcode).collect { resource ->
            _foodProductResource.postValue(resource)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getWiFiSuggestionsList(
        ssid: String,
        securityType: String,
        password: String?
    ): ArrayList<WifiNetworkSuggestion> =
        connectivityHelper.getWiFiSuggestionsList(ssid, securityType, password)

}