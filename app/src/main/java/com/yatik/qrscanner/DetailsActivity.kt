package com.yatik.qrscanner

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.database.BarcodeData
import com.yatik.qrscanner.databinding.ActivityDetailsBinding
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private val barcodeViewModel: BarcodeViewModel by viewModels {
        BarcodeViewModelFactory((application as BarcodeDataApplication).repository)
    }

    /*
    * SSID, title, text, number, phone_number, raw => title: String
    *
    * password, url, message => decryptedText: String
    *
    * encryptionType, ($latitude,$longitude) => others: String
    *
    * */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val valueType = intent.getIntExtra("valueType", -1)
        val title = intent.getStringExtra("title")
        val decryptedText = intent.getStringExtra("decryptedText")
        val others = intent.getStringExtra("others")
        val retrievedFrom = intent.getStringExtra("retrievedFrom") ?: "QR"
        getBarcodeDetails(valueType, title, decryptedText, others, retrievedFrom)
    }

    @SuppressLint("SetTextI18n")
    private fun getBarcodeDetails(valueType: Int, title: String?, decryptedText: String?, others: String?, retrievedFrom: String) {

        val rawValue = title ?: "Sorry, this QR code doesn't contain any data"

        val answer: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            formatter.format(date)
        }

        when (valueType) {
            Barcode.TYPE_WIFI -> {
                binding.typeIcon.setImageResource(R.drawable.outline_wifi_24)
                binding.typeText.text = "Wifi"
                binding.decodedText.text = String.format(
                    "SSID: %s\n\nPassword: %s\n\nType: %s", title, decryptedText, others
                )
                binding.launchButton.visibility = View.GONE
                binding.wifiButton.visibility = View.VISIBLE
                binding.wifiButton.setOnClickListener { openWifiSettings() }
            }

            Barcode.TYPE_URL -> {
                binding.typeIcon.setImageResource(R.drawable.outline_url_24)
                binding.typeText.setText(R.string.url)
                binding.decodedText.text = String.format(
                    "Title: %s\n\nUrl: %s",
                    title,
                    decryptedText
                )
                binding.launchButton.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(decryptedText)))
                }
            }

            Barcode.TYPE_TEXT -> {
                binding.decodedText.text = title
                if (title!!.startsWith("upi://pay")) {
                    binding.typeIcon.setImageResource(R.drawable.upi_24)
                    binding.typeText.text = getString(R.string.upi)
                    payViaUPI(title)
                    binding.launchButton.setOnClickListener { payViaUPI(title) }
                } else {
                    binding.typeIcon.setImageResource(R.drawable.outline_text_icon)
                    binding.typeText.setText(R.string.text)
                    binding.launchButton.setOnClickListener { shareData(title) }
                }
            }

            Barcode.TYPE_PHONE -> {
                binding.typeIcon.setImageResource(R.drawable.outline_call_24)
                binding.typeText.setText(R.string.phone)
                binding.decodedText.text = String.format("Phone. No: %s", title)
                binding.launchButton.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", title, null)))
                }
            }

            Barcode.TYPE_GEO -> {
                val longLat = others?.split(",")
                val latitude = longLat?.get(0)
                val longitude = longLat?.get(1)
                binding.typeIcon.setImageResource(R.drawable.outline_location_24)
                binding.typeText.text = "Location"
                binding.decodedText.text = String.format("Latitude: %s\n\nLongitude: %s", latitude, longitude)
                binding.launchButton.setOnClickListener {
                    val gmmIntentUri = Uri.parse(String.format("geo:%s,%s", latitude, longitude))
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (packageManager != null) {
                        startActivity(mapIntent)
                    } else {
                        Toast.makeText(this, "Google Maps is not installed on your device", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Barcode.TYPE_SMS -> {
                binding.typeIcon.setImageResource(R.drawable.outline_sms_24)
                binding.typeText.setText(R.string.sms)
                binding.decodedText.text = String.format(
                    "Phone. No: %s\n\nMessage: %s", title, decryptedText
                )
                binding.launchButton.setOnClickListener {
                    shareData(decryptedText ?: "")
                }
                binding.typeIcon.setImageResource(R.drawable.outline_text_icon)
                binding.typeText.setText(R.string.raw)
                binding.decodedText.text = rawValue
                binding.launchButton.setOnClickListener {
                    shareData(rawValue)
                }
            }

            else -> {
                binding.typeIcon.setImageResource(R.drawable.outline_question_mark_24)
                binding.typeText.setText(R.string.raw)
                binding.decodedText.text = rawValue
                binding.launchButton.setOnClickListener {
                    shareData(rawValue)
                }
            }
        }

        binding.copyButton.setOnClickListener {
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            copyData((binding.decodedText.text as String))
        }
        binding.shareButton.setOnClickListener {
            shareData(
                (binding.decodedText.text as String)
            )
        }

        if (retrievedFrom == "QR") {
            val barcodeData = BarcodeData(valueType, title, decryptedText, others, answer)
            barcodeViewModel.insert(barcodeData)
        }

    }

    private fun copyData(text: String) {
        val clipboardManager =
            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copied", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun shareData(text: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun payViaUPI(ID: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ID))
        val payIntent = Intent.createChooser(intent, "Pay with:")
        startActivity(payIntent)
    }

    private fun openWifiSettings() {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

}
