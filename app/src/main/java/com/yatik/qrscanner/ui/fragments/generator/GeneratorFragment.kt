package com.yatik.qrscanner.ui.fragments.generator

import android.Manifest
import android.annotation.SuppressLint
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
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentGeneratorBinding
import com.yatik.qrscanner.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * A fragment that generates different types of QR codes based on the
 * supplied data.
 *
 * To use this fragment, pass the GeneratorData object with 'type' variable,
 * and set the corresponding data for that type. For example, to generate a WiFi QR code, set the
 * `type` to `Barcode.TYPE_WIFI` and set the `ssid`, `password`, and `securityType`
 * variables of GeneratorData object to the appropriate values.
 *
 * - Use the key 'GeneratorData' in `putParcelable(key: String, value: GeneratorData)` method
 *   and pass it as a bundle.
 *
 * Supported types of QR codes:
 * - `Barcode.TYPE_TEXT`: Plain text QR code. Pass the GeneratorData object with `text` variable.
 * - `Barcode.TYPE_WIFI`: WiFi network configuration QR code. Pass the GeneratorData object with `ssid`, `password`, and
 *   `securityType` variables.
 * - `Barcode.TYPE_URL`: URL QR code. Pass the GeneratorData object with `url` variable.
 * - `Barcode.TYPE_SMS`: SMS QR code. Pass the GeneratorData object with `phone` and `message` variables.
 * - `Barcode.TYPE_PHONE`: Phone number QR code. Pass the GeneratorData object with `phone` variable.
 * - `Barcode.TYPE_EAN_13`: EAN-13 barcode. Pass the GeneratorData object with `barcodeNumber` variable.
 */

@AndroidEntryPoint
class GeneratorFragment : Fragment() {

    private var _binding: FragmentGeneratorBinding? = null
    private val binding get() = _binding!!
    private val generatorViewModel: GeneratorViewModel by viewModels()
    private val args: GeneratorFragmentArgs by navArgs()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

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
            setBarcodeInfo()
        }
        generatorViewModel.isQRGeneratedSuccessfully.observe(viewLifecycleOwner) { success ->
            if (!success) {
                Toast.makeText(requireContext(), "Sorry, something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
        generatorViewModel.imageSaved.observe(viewLifecycleOwner) { isSaved ->
            if (isSaved) {
                Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Sorry, Unable to save this image", Toast.LENGTH_SHORT).show()
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
        generatorViewModel.generateQRCode(args.GeneratorData)
    }

    private fun saveImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            generatorViewModel.saveImageToGallery()
        } else {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) -> {
                    generatorViewModel.saveImageToGallery()
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun noPermissionDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Permission Denied!")
            .setMessage(R.string.permissionDeniedMessageSaveImage)
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
        dialog.makeButtonTextBlue()
    }

    private fun AlertDialog.makeButtonTextBlue() {
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(context,
                R.color.dialogButtons
            ))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(context,
                R.color.dialogButtons
            ))
    }

    @SuppressLint("SetTextI18n")
    private fun setBarcodeInfo() {
        val generatorData = args.GeneratorData
        when (generatorData.type) {
            Barcode.TYPE_TEXT -> binding.barcodeInfoTv.text = generatorData.text
            Barcode.TYPE_WIFI -> {
                val info = "SSID: ${generatorData.ssid}\n\n" +
                        "Security: ${generatorData.securityType}\n\n" +
                        "Password: ${generatorData.password}"
                binding.barcodeInfoTv.text = info
            }
            Barcode.TYPE_URL -> binding.barcodeInfoTv.text = generatorData.url
            Barcode.TYPE_SMS -> {
                val info = "Phone: ${generatorData.phone}\n\n" +
                        "Message: ${generatorData.message}"
                binding.barcodeInfoTv.text = info
            }
            Barcode.TYPE_PHONE -> binding.barcodeInfoTv.text = generatorData.phone
            Barcode.FORMAT_EAN_13 -> binding.barcodeInfoTv.text = generatorData.barcodeNumber
            else -> binding.barcodeInfoTv.text = "No data available to show"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}