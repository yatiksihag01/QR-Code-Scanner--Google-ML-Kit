package com.yatik.qrscanner.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentGeneratorBinding
import com.yatik.qrscanner.models.GeneratorData
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fragment that generates different types of QR codes based on the
 * supplied data.
 *
 * To use this fragment, set the type of QR code to generate using the `type` instance variable,
 * and set the corresponding data for that type. For example, to generate a WiFi QR code, set the
 * `type` to `Barcode.TYPE_WIFI` and set the `ssid`, `password`, and `securityType` instance
 * variables to the appropriate values.
 *
 * Supported types of QR codes:
 * - `Barcode.TYPE_TEXT`: Plain text QR code. Set the `text` instance variable to the text to encode.
 * - `Barcode.TYPE_WIFI`: WiFi network configuration QR code. Set the `ssid`, `password`, and
 *   `securityType` instance variables to the network configuration values.
 * - `Barcode.TYPE_URL`: URL QR code. Set the `url` instance variable to the URL to encode.
 * - `Barcode.TYPE_SMS`: SMS QR code. Set the `phone` and `message` instance variables to the
 *   recipient phone number and message text, respectively.
 * - `Barcode.TYPE_PHONE`: Phone number QR code. Set the `phone` instance variable to the phone
 *   number to encode.
 * - `Barcode.TYPE_EAN_13`: EAN-13 barcode. Set the `barcodeNumber` instance variable to the
 *   barcode number to encode.
 */

@AndroidEntryPoint
class GeneratorFragment : Fragment() {

    var type: Int = -1
    var text: String? = null
    var url: String? = null
    var ssid: String? = null
    var securityType: String? = null
    var password: String? = null
    var phone: String? = null
    var message: String? = null
    var barcodeNumber: String? = null

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!
    private val generatorViewModel: GeneratorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generatorViewModel.bitmap.observe(viewLifecycleOwner) { bitmap ->
            binding.qrFinalImage.setImageBitmap(bitmap)
            if (!generatorViewModel.isQRGeneratedSuccessfully) {
                Toast.makeText(requireContext(), "Sorry, something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
        binding.generatorToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save_file -> {
                    generatorViewModel.saveImageToGallery()
                    if (generatorViewModel.imageSaved) {
                        Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Sorry, Unable to save this file", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
        val generatorData = GeneratorData(type, text, url, ssid, securityType, password, phone, message, barcodeNumber)
        generatorViewModel.generateQRCode(generatorData)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val TAG = "GeneratorFragment"
    }

}