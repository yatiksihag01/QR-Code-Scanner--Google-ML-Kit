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

package com.yatik.qrscanner.ui.fragments.generator

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentGeneratorBinding
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.data.WiFi
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import com.yatik.qrscanner.ui.MainActivity
import com.yatik.qrscanner.utils.Utilities.Companion.makeButtonTextTeal
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fragment that generates different types of QR codes based on the
 * supplied data.
 *
 * To use this fragment, pass the [BarcodeDetails] object with [BarcodeDetails.format]
 * as [Format.QR_CODE] or [Format.EAN_13], and set the corresponding data for that [Type].
 * For example, to generate a WiFi QR code, set the
 * `type` to [Type.TYPE_WIFI] and set the [WiFi.ssid], [WiFi.password], and [WiFi.security]
 * variables of BarcodeDetails object to the appropriate values.
 *
 * - Use the key 'barcodeDetails' in `putParcelable(key: String, value: BarcodeDetails)` method
 *   and pass it as a bundle.
 *
 * Supported types of QR codes:
 * - [Type.TYPE_TEXT]: Plain text QR code.
 * - [Type.TYPE_WIFI]: WiFi network configuration QR code.
 * - [Type.TYPE_URL]: URL QR code.
 * - [Type.TYPE_SMS]: SMS QR code.
 * - [Type.TYPE_PHONE]: Phone number QR code.
 * - [Format.EAN_13]: EAN-13 barcode.
 */

@AndroidEntryPoint
class GeneratorFragment : Fragment() {

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!
    private val generatorViewModel: GeneratorViewModel by viewModels()
    private val args: GeneratorFragmentArgs by navArgs()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generatorViewModel.bitmap.observe(viewLifecycleOwner) { bitmap ->
            binding.qrFinalImage.setImageBitmap(bitmap)
            setBarcodeInfo()
        }
        generatorViewModel.isQRGeneratedSuccessfully.observe(viewLifecycleOwner) { success ->
            if (!success) {
                Toast.makeText(requireContext(), "Sorry, something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        generatorViewModel.imageSaved.observe(viewLifecycleOwner) { isSaved ->
            if (isSaved) {
                Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    requireContext(), "Sorry, Unable to save this image", Toast.LENGTH_SHORT
                ).show()
            }
        }
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                generatorViewModel.saveImageToGallery()
            } else {
                noPermissionDialog()
            }
        }
        binding.generatorToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.save_file -> {
                    saveImage()
                    true
                }

                else -> false
            }
        }
        binding.generatorToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }
        generatorViewModel.generateBarcode(args.barcodeDetails)
    }

    private fun saveImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            generatorViewModel.saveImageToGallery()
        } else {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> {
                    generatorViewModel.saveImageToGallery()
                }

                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }

    private fun noPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Denied!").setMessage(R.string.permissionDeniedMessageSaveImage)
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setPositiveButton("Allow") { _: DialogInterface?, _: Int ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                this.startActivity(intent)
            }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background)
        )
        dialog.show()
        dialog.makeButtonTextTeal(requireContext())
    }

    private fun setBarcodeInfo() {
        val barcodeDetails = args.barcodeDetails
        when (barcodeDetails.format) {
            Format.EAN_8, Format.EAN_13, Format.UPC_A, Format.UPC_E -> {
                binding.barcodeInfoTv.text = barcodeDetails.rawValue
                binding.barcodeTypeTv.text = getString(R.string.product)
            }

            Format.CODABAR, Format.CODE_39, Format.CODE_93, Format.CODE_128 -> {
                binding.barcodeInfoTv.text = barcodeDetails.rawValue
                binding.barcodeTypeTv.text = barcodeDetails.format.toString()
            }

            Format.QR_CODE -> {
                setQRInfo(barcodeDetails)
            }

            else -> {
                binding.barcodeInfoTv.text = barcodeDetails.rawValue
                binding.barcodeTypeTv.text = barcodeDetails.format.toString()
            }
        }

    }

    private fun setQRInfo(barcodeDetails: BarcodeDetails) {
        when (barcodeDetails.type) {
            Type.TYPE_TEXT -> {
                binding.barcodeInfoTv.text = barcodeDetails.text
                binding.barcodeTypeTv.text = getString(R.string.text)
            }

            Type.TYPE_WIFI -> {
                val info =
                    "SSID: ${barcodeDetails.wiFi?.ssid}\n\n" +
                            "Security: ${barcodeDetails.wiFi?.security}\n\n" +
                            "Password: ${barcodeDetails.wiFi?.password}"
                binding.barcodeInfoTv.text = info
                binding.barcodeTypeTv.text = getString(R.string.wifi)
            }

            Type.TYPE_URL -> {
                binding.barcodeInfoTv.text = barcodeDetails.url?.url
                binding.barcodeTypeTv.text = getString(R.string.url)
            }

            Type.TYPE_SMS -> {
                val info = "Phone: ${barcodeDetails.phone?.number}\n\n" +
                        "Message: ${barcodeDetails.sms?.message}"
                binding.barcodeInfoTv.text = info
                binding.barcodeTypeTv.text = getString(R.string.sms)
            }

            Type.TYPE_PHONE -> {
                binding.barcodeInfoTv.text = barcodeDetails.phone?.number
                binding.barcodeTypeTv.text = getString(R.string.phone)
            }

            else -> {
                binding.barcodeInfoTv.text = barcodeDetails.rawValue
                binding.barcodeTypeTv.text = barcodeDetails.type.toString()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}