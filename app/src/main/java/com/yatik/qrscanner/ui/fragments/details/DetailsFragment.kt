package com.yatik.qrscanner.ui.fragments.details

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentDetailsBinding
import com.yatik.qrscanner.ui.MainActivity
import com.yatik.qrscanner.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: DetailsFragmentArgs by navArgs()
    private val detailsViewModel: DetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBarcodeDetails()

        binding.detailsToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun getBarcodeDetails() {

        val barcodeData = args.barcodeData
        val format = barcodeData.format
        val valueType = barcodeData.type
        val title = barcodeData.title
        val decryptedText = barcodeData.decryptedText
        val others = barcodeData.others

        val rawValue = title ?: "Sorry, this QR code doesn't contain any data"
        val utilities = Utilities()

        when (format) {
            Barcode.FORMAT_QR_CODE -> {
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
                        if (!decryptedText.isNullOrBlank())
                            setUrlView(decryptedText)
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
                            startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", title, null)
                                )
                            )
                        }
                    }

                    Barcode.TYPE_GEO -> {
                        val longLat = others?.split(",")
                        val latitude = longLat?.get(0)
                        val longitude = longLat?.get(1)
                        binding.typeIcon.setImageResource(R.drawable.outline_location_24)
                        binding.typeText.text = "Location"
                        binding.decodedText.text =
                            String.format("Latitude: %s\n\nLongitude: %s", latitude, longitude)
                        binding.launchButton.setOnClickListener {
                            val gmmIntentUri =
                                Uri.parse(String.format("geo:%s,%s", latitude, longitude))
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            if (requireActivity().packageManager != null) {
                                startActivity(mapIntent)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Google Maps is not installed on your device",
                                    Toast.LENGTH_SHORT
                                ).show()
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
            }

            Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E, Barcode.FORMAT_EAN_8, Barcode.FORMAT_EAN_13, Barcode.TYPE_ISBN -> {
                binding.typeIcon.setImageResource(R.drawable.outline_product_24)
                binding.typeText.text = "Product"
                binding.decodedText.text = title
                binding.extraInfo.visibility = View.VISIBLE
                binding.extraInfo.setText(R.string.productMessage)
                binding.launchButton.setOnClickListener {
                    utilities.customTabBuilder(
                        requireContext(),
                        Uri.parse("https://www.google.com/search?q=$title")
                    )
                }
            }

            else -> {
                binding.typeIcon.setImageResource(R.drawable.outline_barcode_24)
                binding.typeText.text = "Barcode"
                binding.decodedText.text = title
                binding.launchButton.setOnClickListener {
                    shareData(rawValue)
                }
            }
        }
        binding.copyButton.setOnClickListener {
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
            val text = binding.decodedText.text.toString()
            copyData((text))
        }
        binding.shareButton.setOnClickListener {
            shareData(
                (binding.decodedText.text.toString())
            )
        }
    }

    private fun setUrlView(url: String) {

        var title: String? = "fetching..."
        val utilities = Utilities()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding.typeIcon.setImageResource(R.drawable.outline_url_24)
        binding.typeText.setText(R.string.url)
        val mainUrl: String = if (!url.startsWith("http")) "https://$url"
        else url

        detailsViewModel.getUrlPreview(mainUrl)
        detailsViewModel.urlPreviewResource.observe(viewLifecycleOwner) { resource ->
            if (!resource.data?.title.isNullOrBlank())
                title = resource.data?.title
            else if (!resource.message.isNullOrBlank()) {
                title = ""
                Toast.makeText(
                    requireContext(), "${resource.message} to fetch title", Toast.LENGTH_SHORT
                ).show()
            }
            binding.decodedText.text = String.format(
                "Title: %s\n\nUrl: %s",
                title, mainUrl
            )
        }
        binding.launchButton.setOnClickListener {
            utilities.customTabBuilder(requireContext(), Uri.parse(mainUrl))
        }
        val openAutomatically =
            sharedPreferences.getBoolean("open_url_preference", false)
        if (openAutomatically) {
            utilities.customTabBuilder(requireContext(), Uri.parse(mainUrl))
        }
    }


    private fun copyData(text: String) {
        val clipboardManager =
            requireActivity().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}