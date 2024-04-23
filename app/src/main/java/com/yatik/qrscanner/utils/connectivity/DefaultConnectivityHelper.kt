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

package com.yatik.qrscanner.utils.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultConnectivityHelper @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityHelper {

    override fun isConnectedToInternet(): Boolean {

        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun getWiFiSuggestionsList(
        ssid: String,
        securityType: String,
        password: String?
    ): ArrayList<WifiNetworkSuggestion> {
        val suggestions = ArrayList<WifiNetworkSuggestion>()
        val builder = WifiNetworkSuggestion.Builder()
        if (securityType == "Open" || password.isNullOrEmpty()) {
            suggestions.add(
                builder.setSsid(ssid)
                    .build()
            )
        } else if (securityType == "WPA2") {
            suggestions.add(
                builder.setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build()
            )
        } else {
            suggestions.add(
                builder.setSsid(ssid)
                    .setWpa3Passphrase(password)
                    .build()
            )
        }
        return suggestions
    }

}