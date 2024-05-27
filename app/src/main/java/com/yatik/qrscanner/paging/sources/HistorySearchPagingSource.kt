package com.yatik.qrscanner.paging.sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yatik.qrscanner.database.BarcodeDao
import com.yatik.qrscanner.models.barcode.BarcodeDetails

class HistorySearchPagingSource(
    private val barcodeDao: BarcodeDao,
    private val pageSize: Int
) : PagingSource<Int, BarcodeDetails>() {

    var searchQuery: String? = null

    /**
     * prevKey == null -> anchorPage is the first page.
     *
     * nextKey == null -> anchorPage is the last page.
     *
     * Both prevKey and nextKey are null -> anchorPage is the initial page, so return null.
     */
    override fun getRefreshKey(state: PagingState<Int, BarcodeDetails>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BarcodeDetails> {
        return try {
            var currentOffset = params.key ?: 0
            val filteredItemsList = mutableListOf<BarcodeDetails>()
            var reachedEnd = false

            while (filteredItemsList.size < pageSize && !reachedEnd) {
                val barcodeDetailsList = barcodeDao.getBarcodePages(pageSize, currentOffset)
                // If barcodeDetailsList is empty, we reached the end of the table
                if (barcodeDetailsList.isEmpty()) {
                    reachedEnd = true
                } else {
                    // To keep record of last fetched item position in table
                    currentOffset += barcodeDetailsList.size
                    filteredItemsList.addAll(
                        barcodeDetailsList.filter { matchesQuery(it, searchQuery ?: "") }
                    )
                    // If filteredItemsList size is less than pageSize, we reached the end of the table
                    if (barcodeDetailsList.size < pageSize) reachedEnd = true
                    if (filteredItemsList.size >= pageSize) break
                }
            }

            LoadResult.Page(
                data = filteredItemsList.take(pageSize),
                prevKey = if (currentOffset < pageSize) null else (currentOffset - pageSize),
                nextKey = if (reachedEnd) null else currentOffset
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    private fun matchesQuery(barcodeDetails: BarcodeDetails, query: String): Boolean {
        return barcodeDetails.text?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.rawValue.contains(query, ignoreCase = true) ||
                barcodeDetails.url?.url?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.phone?.number?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.phone?.name?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.sms?.message?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.sms?.message?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.wiFi?.ssid?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.wiFi?.password?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.email?.email?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.isbn?.contains(query, ignoreCase = true) == true ||
                barcodeDetails.calendarEvent?.organizer?.contains(query, ignoreCase = true) == true

    }
}