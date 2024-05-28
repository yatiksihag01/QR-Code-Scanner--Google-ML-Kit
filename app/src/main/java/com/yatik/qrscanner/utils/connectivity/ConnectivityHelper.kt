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

import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi

interface ConnectivityHelper {

    fun isConnectedToInternet(): Boolean

    /**
     * Retrieves a list of [WifiNetworkSuggestion] object for the given SSID, security type, and
     * password (if provided).
     * If the password is null or empty, an open network suggestion is created.
     * If a password is provided, a WPA2 or WPA3 network suggestion is created as per given securityType.
     *
     * Note: Requires API level [Build.VERSION_CODES.R] or higher.
     *
     * @param ssid The SSID (network name) of the WiFi network.
     * @param securityType The security type of the WiFi network (e.g., "WPA2", "WPA3", "OPEN").
     * @param password The password for the WiFi network. Can be null for open networks.
     * @return An ArrayList of [WifiNetworkSuggestion] objects representing the WiFi network suggestions.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun getWiFiSuggestionsList(
        ssid: String,
        securityType: String,
        password: String?
    ): ArrayList<WifiNetworkSuggestion>

}