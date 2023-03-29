package com.yatik.qrscanner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.ActivityMainBinding
import com.yatik.qrscanner.ui.fragments.HistoryFragment
import com.yatik.qrscanner.utils.Constants
import com.yatik.qrscanner.utils.Constants.Companion.CAMERA_REQUEST_CODE
import com.yatik.qrscanner.utils.Constants.Companion.SHEET_PEEK_VAL
import com.yatik.qrscanner.utils.DialogUtils
import com.yatik.qrscanner.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _mCamera: Camera? = null
    private val mCamera get() = _mCamera!!

    private var isImageSelected = false
    private var isClickedAllowButton = false

    private lateinit var binding: ActivityMainBinding
    private var mChoosePhoto: ActivityResultLauncher<String>? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private lateinit var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val barcodeViewModel: BarcodeViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val utilities = Utilities()
    private val tag = "MainActivityTag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        utilities.hideSystemBars(window, this)

        mCameraProviderFuture = ProcessCameraProvider.getInstance(this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)
        if (supportFragmentManager.backStackEntryCount > 0) {
            collapseBottomSheet()
        } else {
            requestCameraPermission()
            bottomSheetBehavior.peekHeight = utilities.calculatePeekHeight(this, SHEET_PEEK_VAL)
            bottomSheetBehavior.isHideable = false
        }
        setBottomSheetButtons()

        mChoosePhoto =
            registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
                if (result != null) {
                    isImageSelected = true
                    val image: InputImage
                    val options = BarcodeScannerOptions.Builder()
                        .build()
                    val scanner = BarcodeScanning.getClient(options)
                    try {
                        image = InputImage.fromFilePath(this, result)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes: List<Barcode> -> processResult(barcodes) }
                        .addOnFailureListener { e: Exception ->
                            // Task failed with an exception
                            Toast.makeText(this@MainActivity, "Failed to scan.", Toast.LENGTH_SHORT)
                                .show()
                            e.printStackTrace()
                        }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.zoomInfo.text = getString(R.string.initial_zoom)
        binding.buttonFlash.setImageResource(R.drawable.outline_flash_off_28)
        if (isClickedAllowButton) {
            isClickedAllowButton = false
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        if (utilities.hasCameraPermission(this))
            setupCamera()
        else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {              // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCamera()
                } else {
                    val message = resources.getString(R.string.permissionDeniedMessageCam)
                    noPermissionDialog(message)
                }
                return
            }
        }
    }

    private fun setupCamera() {
        mCameraProvider?.unbindAll()
        _mCamera = null
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
        }, ContextCompat.getMainExecutor(this))
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun processScan() {
        val preview = Preview.Builder().build()
        val imageAnalysis = ImageAnalysis.Builder().build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy: ImageProxy ->

            val image = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .build()
            val scanner = BarcodeScanning.getClient(options)
            scanner.process(image)
                .addOnSuccessListener { barcodes: List<Barcode> -> processResult(barcodes) }
                .addOnFailureListener { e: Exception ->
                    // Task failed with an exception
                    Toast.makeText(this, "Failed to scan.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
                .addOnCompleteListener(ContextCompat.getMainExecutor(this)) { imageProxy.close() }
        }

        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)
        val useFrontCam = sharedPreferences.getBoolean("front_cam_preference", false)
        val hasFrontCamera = mCameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

        val cameraSelector = if (useFrontCam && hasFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        _mCamera = mCameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        zoomControl()
        flashControl()
    }

    private fun processResult(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            mCameraProvider?.unbindAll()
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this)
            val isVibrationAllowed = sharedPreferences.getBoolean("vibration_preference", true)
            utilities.vibrateIfAllowed(this, isVibrationAllowed, 100)
            sendRequiredData(barcodes[0])
            isImageSelected = false
            setupCamera()
        } else if (isImageSelected) {
            Toast.makeText(this, "Failed to scan", Toast.LENGTH_SHORT).show()
            isImageSelected = false
        }
    }

    private fun sendRequiredData(barcode: Barcode){
        val barcodeData = utilities.barcodeToBarcodeData(barcode)
        val saveScan = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("save_scans_preference", true)
        if (saveScan) barcodeViewModel.insert(barcodeData)

        intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("barcodeData", barcodeData)
        startActivity(intent)
    }

    private fun setBottomSheetButtons() {
        binding.dragHandle.setOnClickListener {
            bottomSheetBehavior.state =
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    BottomSheetBehavior.STATE_EXPANDED
                } else BottomSheetBehavior.STATE_COLLAPSED
        }
        binding.buttonGallery.setOnClickListener { mChoosePhoto!!.launch("image/*") }
        binding.buttonHistory.setOnClickListener {
            mCameraProvider?.unbindAll()
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack("historyFrag")
                setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                add<HistoryFragment>(R.id.main_layout)
            }
            collapseBottomSheet()
        }
        binding.buttonSettingsMain.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.ratingButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            DialogUtils().ratingDialog(this)
        }
        binding.shareButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(
                Intent.EXTRA_TEXT,
                Constants.SHARE_APP_MESSAGE
            )
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share app via"))
        }
        binding.policyButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Utilities().customTabBuilder(
                this@MainActivity,
                Uri.parse(Constants.POLICIES_LINK)
            )
        }
    }

    private fun zoomControl() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @SuppressLint("SetTextI18n")
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = mCamera.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                mCamera.cameraControl.setZoomRatio(scale)
                if (scale in 1.0..mCamera.cameraInfo.zoomState.value!!.maxZoomRatio.toDouble()) {
                    binding.zoomInfo.text = String.format("%.1f", scale) + "x"
                }
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this@MainActivity, listener)
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
                    binding.buttonFlash.setImageResource(R.drawable.outline_flash_on_28)
                } else {
                    mCamera.cameraControl.enableTorch(false)
                    binding.buttonFlash.setImageResource(R.drawable.outline_flash_off_28)
                }
            } else {
                Toast.makeText(
                    this, "No flashlight found for this camera", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun noPermissionDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Denied!")
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setPositiveButton("Allow") { _: DialogInterface?, _: Int ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                this.startActivity(intent)
                isClickedAllowButton = true
            }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(this, R.drawable.dialog_background)
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

    private fun collapseBottomSheet() {
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }


    private var doublePressToExit = false
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val supportFragmentManager = supportFragmentManager
        if (supportFragmentManager.backStackEntryCount > 0) {
            bottomSheetBehavior.peekHeight = utilities.calculatePeekHeight(this, SHEET_PEEK_VAL)
            binding.buttonFlash.setImageResource(R.drawable.outline_flash_off_28)
            requestCameraPermission()
            supportFragmentManager.popBackStack()
            return
        } else if (doublePressToExit) {
            super.onBackPressed()
            return
        }
        doublePressToExit = true
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doublePressToExit = false }, 2000)
    }

}
