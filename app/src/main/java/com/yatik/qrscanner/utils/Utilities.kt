package com.yatik.qrscanner.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.models.BarcodeData
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
    }

    fun hideSystemBars(window: android.view.Window, context: Context, hideStatusBar: Boolean) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide the status bar
        if (hideStatusBar) {
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        }
        val lightBlue = context.getString(R.string.light_blue_int_val)
        val darkBlue = context.getString(R.string.dark_blue_int_val)
        val lightGreen = context.getString(R.string.light_green_int_val)
        val darkGreen = context.getString(R.string.dark_green_int_val)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val currentTheme = sharedPref.getString(
            context.getString(R.string.theme_preference_key),
            context.getString(R.string.light_blue_int_val)
        )
        when (currentTheme) {
            lightBlue -> {
                windowInsetsController.isAppearanceLightStatusBars = true
                windowInsetsController.isAppearanceLightNavigationBars = true
            }
            darkBlue -> {
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            }
            lightGreen -> {
                windowInsetsController.isAppearanceLightStatusBars = true
                windowInsetsController.isAppearanceLightNavigationBars = true
            }
            darkGreen -> {
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            }
        }
        window.navigationBarColor =
            context.getColorFromAttr(com.google.android.material.R.attr.colorPrimaryVariant)

        // Make navigation bar transparent
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
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

}