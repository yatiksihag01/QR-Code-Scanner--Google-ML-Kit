package com.yatik.qrscanner.ui.fragments.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yatik.qrscanner.models.UrlPreviewData
import com.yatik.qrscanner.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: DetailsRepository
) : ViewModel() {

    private val _urlPreviewResource: MutableLiveData<Resource<UrlPreviewData>> = MutableLiveData()
    val urlPreviewResource: LiveData<Resource<UrlPreviewData>>
        get() = _urlPreviewResource

    fun getUrlPreview(url: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.getUrlInfo(url).collect { resource ->
            withContext(Dispatchers.Main) {
                _urlPreviewResource.postValue(resource)
            }
        }
    }

}