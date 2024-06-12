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

package com.yatik.qrscanner.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentHomeBinding
import com.yatik.qrscanner.ui.SettingsActivity
import com.yatik.qrscanner.ui.fragments.cropper.CropperFragment
import com.yatik.qrscanner.ui.fragments.history.BarcodeViewModel
import com.yatik.qrscanner.utils.Constants
import com.yatik.qrscanner.utils.Constants.Companion.SHEET_PEEK_VAL
import com.yatik.qrscanner.utils.PermissionHelper
import com.yatik.qrscanner.utils.Utilities
import com.yatik.qrscanner.utils.mappers.Mapper
import com.yatik.qrscanner.utils.ratingDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutionException

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _mCamera: Camera? = null
    private val mCamera get() = _mCamera!!

    private lateinit var pickVisualMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var mCameraProvider: ProcessCameraProvider? = null
    private lateinit var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val barcodeViewModel: BarcodeViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val utilities = Utilities()
    private val permissionHelper = PermissionHelper()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)
        bottomSheetBehavior.peekHeight =
            utilities.calculatePeekHeight(requireContext(), SHEET_PEEK_VAL)
        bottomSheetBehavior.isHideable = false
        permissionHelper.requestCameraPermission(requireContext(), this)

        mCameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        pickVisualMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result: Uri? ->
                if (result != null) {
                    val bundle = Bundle().also {
                        it.putString(CropperFragment.ARG_KEY, result.toString())
                    }
                    findNavController().navigate(
                        R.id.action_homeFragment_to_cropperFragment, bundle
                    )
                }
            }
        setBottomSheetButtons()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    override fun onResume() {
        super.onResume()
        binding.zoomInfo.text = getString(R.string.initial_zoom)
        binding.buttonFlash.setIconResource(R.drawable.outline_flash_off_28)
        if (permissionHelper.isCameraPermissionGranted(requireContext())) {
            setupCamera()
        }
    }

    private fun setupCamera() {
        mCameraProvider?.unbindAll()
        mCameraProviderFuture.addListener({
            try {
                mCameraProvider = mCameraProviderFuture.get()
                processScan()
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.d(tag, "An error occurred: $e")

            } catch (e: InterruptedException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.d(tag, "An error occurred: $e")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processScan() {
        val preview = Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder().build()

        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(requireContext())
        ) { imageProxy: ImageProxy ->
            val image =
                InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder().build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes: List<Barcode> -> processResult(barcodes) }
                .addOnFailureListener { e: Exception ->
                    // Task failed with an exception
                    Toast.makeText(
                        requireContext(),
                        "Failed to scan.",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
                .addOnCompleteListener(
                    ContextCompat.getMainExecutor(requireContext())
                ) { imageProxy.close() }
        }

        val useFrontCam = sharedPreferences.getBoolean("front_cam_preference", false)
        val hasFrontCamera =
            mCameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

        val cameraSelector = if (useFrontCam && hasFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        _mCamera = mCameraProvider?.bindToLifecycle(
            this,
            cameraSelector,
            imageAnalysis,
            preview
        )

        zoomControl()
        flashControl()
    }

    private fun processResult(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            mCameraProvider?.unbindAll()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val isVibrationAllowed = sharedPreferences.getBoolean("vibration_preference", true)
            utilities.vibrateIfAllowed(requireContext(), isVibrationAllowed, 100)
            sendRequiredData(barcodes[0])
            binding.homeProgressBar.visibility = View.GONE
        }
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
            R.id.action_homeFragment_to_detailsFragment, bundle
        )
    }

    private fun setBottomSheetButtons() {
        binding.buttonGallery.setOnClickListener {
            pickVisualMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
        binding.buttonHistory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        binding.buttonCreateQr.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_QRCodeGeneratorFragment)
            collapseBottomSheet()
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.ratingButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            ratingDialog(requireContext()) {}
        }
        binding.shareButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(
                Intent.EXTRA_TEXT, Constants.SHARE_APP_MESSAGE
            )
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share app via"))
        }
        binding.policyButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Utilities().customTabBuilder(
                requireContext(), Uri.parse(Constants.POLICIES_LINK)
            )
        }
    }

    private fun zoomControl() {
        val maxZoom = mCamera.cameraInfo.zoomState.value!!.maxZoomRatio.toDouble()
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @SuppressLint("SetTextI18n")
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = mCamera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                mCamera.cameraControl.setZoomRatio(scale)
                if (scale in 1.0..maxZoom) {
                    binding.zoomInfo.text = String.format("%.1f", scale) + "x"
                }
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(requireContext(), listener)
        binding.previewView.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            view.performClick()
            return@setOnTouchListener true
        }
    }

    private fun flashControl() {
        binding.buttonFlash.setOnClickListener {
            if (mCamera.cameraInfo.hasFlashUnit()) {
                if (mCamera.cameraInfo.torchState.value == TorchState.OFF) {
                    mCamera.cameraControl.enableTorch(true)
                    binding.buttonFlash.setIconResource(R.drawable.outline_flash_on_28)
                } else {
                    mCamera.cameraControl.enableTorch(false)
                    binding.buttonFlash.setIconResource(R.drawable.outline_flash_off_28)
                }
            } else {
                Toast.makeText(
                    requireContext(), "No flashlight found for this camera", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun collapseBottomSheet() {
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onPause() {
        super.onPause()
        mCameraProvider?.unbindAll()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}