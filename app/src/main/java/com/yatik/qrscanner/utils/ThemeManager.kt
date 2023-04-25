package com.yatik.qrscanner.utils

import android.app.Activity
import androidx.preference.PreferenceManager
import com.yatik.qrscanner.R
import com.yatik.qrscanner.utils.Utilities.Companion.isSystemThemeLight

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