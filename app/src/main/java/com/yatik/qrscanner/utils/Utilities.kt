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

package com.yatik.qrscanner.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Window
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.yatik.qrscanner.R
import com.yatik.qrscanner.models.BarcodeData
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Utilities {

    companion object {
        @ColorInt
        fun Context.getColorFromAttr(
            @AttrRes attrColor: Int
        ): Int {
            val typedArray = theme.obtainStyledAttributes(intArrayOf(attrColor))
            val textColor = typedArray.getColor(0, 0)
            typedArray.recycle()
            return textColor
        }

        fun AlertDialog.makeButtonTextTeal(context: Context) {
            this.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    context.getColorFromAttr(
                        com.google.android.material.R.attr.colorSecondaryVariant
                    )
                )
            this.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                    context.getColorFromAttr(
                        com.google.android.material.R.attr.colorSecondaryVariant
                    )
                )
        }

        fun isSystemThemeLight(activity: Activity): Boolean =
            activity.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO
    }

    fun setSystemBars(window: Window, activity: Activity, hideStatusBar: Boolean) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide the status bar
        if (hideStatusBar) {
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        }
        val lightBlue = activity.getString(R.string.light_blue_int_val)
        val darkBlue = activity.getString(R.string.dark_blue_int_val)
        val lightGreen = activity.getString(R.string.light_green_int_val)
        val darkGreen = activity.getString(R.string.dark_green_int_val)
        val systemDefault = activity.getString(R.string.system_default_int_val)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        val currentTheme = sharedPref.getString(
            activity.getString(R.string.theme_preference_key),
            activity.getString(R.string.system_default_int_val)
        )
        when (currentTheme) {
            lightBlue -> lightBars(true, window)
            darkBlue -> lightBars(false, window)
            lightGreen -> lightBars(true, window)
            darkGreen -> lightBars(false, window)
            systemDefault -> {
                if (isSystemThemeLight(activity)) lightBars(true, window)
                else lightBars(false, window)
            }
        }
        window.navigationBarColor =
            activity.getColorFromAttr(com.google.android.material.R.attr.colorPrimaryVariant)

        // Make navigation bar transparent
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
    }

    private fun lightBars(makeBarsLight: Boolean, window: Window) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (makeBarsLight) {
            windowInsetsController.isAppearanceLightStatusBars = true
            windowInsetsController.isAppearanceLightNavigationBars = true
        } else {
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    fun vibrateIfAllowed(context: Context, isVibrationAllowed: Boolean, timeInMillis: Long) {
        if (isVibrationAllowed) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(timeInMillis, 125))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timeInMillis)
            }
        }
    }

    fun customTabBuilder(context: Context, uri: Uri) {
        val stringUri = uri.toString()
        val newUri = if (!stringUri.startsWith("https://") && !stringUri.startsWith("http://")) {
            Uri.parse("https://$stringUri")
        } else uri
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(context.getColorFromAttr(com.google.android.material.R.attr.colorPrimaryVariant))
            .setNavigationBarColor(context.getColorFromAttr(com.google.android.material.R.attr.colorPrimaryVariant))
            .build()

        val builder = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(defaultColors)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, newUri)
    }

    fun calculatePeekHeight(context: Context, peekHeightDp: Int): Int {
        val metrics = context.resources.displayMetrics
        val density = metrics.densityDpi / 160f
        return (peekHeightDp * density).toInt()
    }

    /*
    * SSID, title, text, number, phone_number, raw, barcodes => title: String
    *
    * password, url, message => decryptedText: String
    *
    * encryptionType, ($latitude,$longitude) => others: String
    *
    * */

    fun barcodeToBarcodeData(barcode: Barcode): BarcodeData {

        val format = barcode.format
        val valueType = barcode.valueType

        val dateTime: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            formatter.format(date)
        }

        var title: String? = null
        var decryptedText: String? = null
        var others: String? = null

        if (barcode.format == Barcode.FORMAT_QR_CODE) {
            when (barcode.valueType) {
                Barcode.TYPE_WIFI -> {
                    title = barcode.wifi?.ssid
                    decryptedText = barcode.wifi?.password
                    others = when (barcode.wifi?.encryptionType) {
                        Barcode.WiFi.TYPE_OPEN -> "Open"
                        Barcode.WiFi.TYPE_WPA -> "WPA"
                        Barcode.WiFi.TYPE_WEP -> "WEP"
                        else -> ""
                    }
                }

                Barcode.TYPE_URL -> {
                    title = barcode.url?.title
                    decryptedText = barcode.url?.url
                }

                Barcode.TYPE_TEXT -> {
                    title = barcode.displayValue
                }

                Barcode.TYPE_PHONE -> {
                    title = barcode.phone?.number
                }

                Barcode.TYPE_GEO -> {
                    val latitude = barcode.geoPoint!!.lat
                    val longitude = barcode.geoPoint!!.lng
                    others = "$latitude,$longitude"
                }

                Barcode.TYPE_SMS -> {
                    title = barcode.sms?.phoneNumber
                    decryptedText = barcode.sms?.message
                }

                else -> {
                    title = barcode.rawValue ?: "Sorry, this QR code doesn't contain any data"
                }
            }
        } else {
            title = barcode.rawValue ?: "Sorry, Something wrong happened. Please try to rescan."
        }

        return BarcodeData(format, valueType, title, decryptedText, others, dateTime)
    }

    /**
     * Processes the provided image [Uri] to detect and extract barcodes or QR codes.
     *
     * @param context The [Context] in which the processing is performed.
     * @param imageUri The [Uri] of the image to be processed.
     * @param result A callback that receives a [List] of [Barcode] objects if barcodes or QR codes
     * are found in the image, or null if none are found.
     */
    fun processUri(context: Context, imageUri: Uri, result: (List<Barcode>?) -> Unit) {
        val options = BarcodeScannerOptions.Builder().build()
        val scanner = BarcodeScanning.getClient(options)
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            scanner.process(image).addOnSuccessListener { barcodes: List<Barcode> ->
                result(barcodes)
            }.addOnFailureListener { e: Exception ->
                // Task failed with an exception
                result(null)
                e.printStackTrace()
            }
        } catch (e: IOException) {
            Toast.makeText(
                context,
                context.getString(R.string.something_went_wrong),
                Toast.LENGTH_SHORT
            ).show()
            result(null)
            e.printStackTrace()
        }
    }

}