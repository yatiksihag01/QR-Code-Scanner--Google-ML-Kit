package com.yatik.qrscanner.utils

import android.app.Activity
import androidx.preference.PreferenceManager
import com.yatik.qrscanner.R
import com.yatik.qrscanner.utils.Utilities.Companion.isSystemThemeLight

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

class ThemeManager {

    companion object {

        fun updateTheme(activity: Activity) {
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity)
            val currentTheme = sharedPreferences.getString(
                activity.getString(R.string.theme_preference_key),
                activity.getString(R.string.system_default_int_val)
            )
            when (currentTheme) {
                activity.getString(R.string.light_blue_int_val) -> activity.setTheme(R.style.Theme_QRScanner)
                activity.getString(R.string.dark_blue_int_val) -> activity.setTheme(R.style.DarkTheme)
                activity.getString(R.string.light_green_int_val) -> activity.setTheme(R.style.LightThemeGreen)
                activity.getString(R.string.dark_green_int_val) -> activity.setTheme(R.style.DarkThemeGreen)
                else -> {
                    if (isSystemThemeLight(activity)) activity.setTheme(R.style.Theme_QRScanner)
                    else activity.setTheme(R.style.DarkTheme)
                }
            }
        }

    }

}