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

package com.yatik.qrscanner.ui.fragments.cropper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.canhub.cropper.CropImageView
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentCropperBinding
import com.yatik.qrscanner.ui.MainActivity
import com.yatik.qrscanner.ui.fragments.history.BarcodeViewModel
import com.yatik.qrscanner.utils.Utilities
import com.yatik.qrscanner.utils.mappers.Mapper
import java.io.File

class CropperFragment : Fragment(), CropImageView.OnSetImageUriCompleteListener,
    CropImageView.OnCropImageCompleteListener {

    private var _binding: FragmentCropperBinding? = null
    private val barcodeViewModel: BarcodeViewModel by activityViewModels()
    private val args: CropperFragmentArgs by navArgs()
    private val binding get() = _binding!!
    private lateinit var cropImageView: CropImageView
    private lateinit var pickVisualMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var imageUri: Uri? = null
    private val utilities = Utilities()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUri = if (savedInstanceState != null) {
            savedInstanceState.getString(ARG_KEY)?.toUri()
        } else args.imageUri.toUri()

        cropImageView = binding.cropImageView
        cropImageView.setOnSetImageUriCompleteListener(this)
        cropImageView.setOnCropImageCompleteListener(this)

        cropImageView.setImageUriAsync(imageUri)
        cropImageView.isShowProgressBar = false

        pickVisualMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result: Uri? ->
                imageUri = result
                cropImageView.setImageUriAsync(imageUri)
            }

        binding.scanCroppedBtn.setOnClickListener {
            cropImageView.croppedImageAsync()
            binding.cropperProgressBar.visibility = View.VISIBLE
        }
        binding.selectAnotherPic.setOnClickListener {
            pickVisualMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
        binding.cropperToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_KEY, imageUri.toString())
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (!result.isSuccessful) {
            Snackbar.make(
                binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        val croppedImageUri = result.getUriFilePath(
            requireContext(), uniqueName = false
        )?.let { File(it).toUri() }

        if (croppedImageUri == null) {
            Snackbar.make(
                view,
                getString(R.string.something_went_wrong),
                Snackbar.LENGTH_SHORT
            ).show()
        } else {
            utilities.processUri(requireContext(), croppedImageUri) { barcodes ->
                if (barcodes.isNullOrEmpty()) {
                    Snackbar.make(
                        view,
                        getString(R.string.barcode_not_found),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    binding.cropperProgressBar.visibility = View.GONE
                } else {
                    processResult(barcodes)
                }
            }
        }
    }

    private fun processResult(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val isVibrationAllowed = sharedPreferences.getBoolean("vibration_preference", true)
            utilities.vibrateIfAllowed(requireContext(), isVibrationAllowed, 100)
            sendRequiredData(barcodes[0])
        }
        binding.cropperProgressBar.visibility = View.GONE
    }

    private fun sendRequiredData(barcode: Barcode) {
        val barcodeDetails = Mapper.fromBarcodeToBarcodeDetails(barcode)
        val saveScan = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean("save_scans_preference", true)
        if (saveScan) barcodeViewModel.insert(barcodeDetails)

        val bundle = Bundle().apply {
            putParcelable("barcodeDetails", barcodeDetails)
        }
        findNavController().navigate(
            R.id.action_cropperFragment_to_detailsFragment, bundle
        )
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Snackbar.make(
                binding.root,
                getString(R.string.image_loading_failed),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val ARG_KEY = "imageUri"
    }
}