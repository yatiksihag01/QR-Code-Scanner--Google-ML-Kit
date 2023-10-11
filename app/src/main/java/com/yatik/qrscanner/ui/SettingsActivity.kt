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

package com.yatik.qrscanner.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PackageInfoFlags
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.yatik.qrscanner.R
import com.yatik.qrscanner.utils.ThemeManager.Companion.updateTheme
import com.yatik.qrscanner.utils.Utilities
import com.yatik.qrscanner.utils.ratingDialog

class SettingsActivity : AppCompatActivity() {

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity)
                val shouldShowRatingDialog =
                    sharedPreferences.getBoolean("shouldShowRatingDialog", true)
                if (shouldShowRatingDialog) {
                    sharedPreferences.edit().putBoolean("shouldShowRatingDialog", false).apply()
                    ratingDialog(this@SettingsActivity) { pressedCancel ->
                        if (pressedCancel) goToMainActivity()
                    }
                } else goToMainActivity()
            }

            private fun goToMainActivity() {
                startActivity(
                    Intent(this@SettingsActivity, MainActivity::class.java)
                )
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTheme(this@SettingsActivity)
        setContentView(R.layout.activity_settings)
        Utilities().setSystemBars(window, this@SettingsActivity, false)

        val settingsToolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        settingsToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
            finish()
        }
        this@SettingsActivity.onBackPressedDispatcher
            .addCallback(this@SettingsActivity, onBackPressedCallback)
    }

    class QRScannerPreferenceFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, null)
            val versionPref = findPreference<Preference>("version_preference")
            try {
                val packageManager = requireActivity().packageManager
                versionPref!!.summary =
                    packageManager.getPackageInfoCompat(requireActivity().packageName).versionName
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            when (key) {
                getString(R.string.theme_preference_key) -> requireActivity().recreate()
            }
        }

        override fun onPause() {
            super.onPause()
            preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }

        private fun PackageManager.getPackageInfoCompat(
            packageName: String,
            flags: Int = 0
        ): PackageInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
            }

    }

}
