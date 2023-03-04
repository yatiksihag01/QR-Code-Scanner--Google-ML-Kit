package com.yatik.qrscanner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.models.BarcodeData
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Utilities {

    fun hideSystemBars(window: android.view.Window, context: Context) {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide the status bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        window.navigationBarColor = context.getColor(R.color.main_background)

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
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(context, R.color.main_background))
            .build()

        val builder = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setDefaultColorSchemeParams(defaultColors)

        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(context, uri)
    }

    fun calculatePeekHeight(context: Context, peekHeightDp: Int): Int {
        val metrics = context.resources.displayMetrics
        val density = metrics.densityDpi / 160f
        return (peekHeightDp * density).toInt()
    }

    fun hasCameraPermission(context: Context) = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED


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