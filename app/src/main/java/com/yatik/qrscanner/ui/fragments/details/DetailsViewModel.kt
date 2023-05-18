package com.yatik.qrscanner.ui.fragments.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.repository.details.DetailsRepository
import com.yatik.qrscanner.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: DetailsRepository
) : ViewModel() {

    private val _urlPreviewResource: MutableLiveData<Resource<UrlPreviewData>> = MutableLiveData()
    val urlPreviewResource: LiveData<Resource<UrlPreviewData>>
        get() = _urlPreviewResource

    fun getUrlPreview(url: String) = viewModelScope.launch {
        repository.getUrlInfo(url).collect { resource ->
            _urlPreviewResource.postValue(resource)
        }
    }

}