package com.yatik.qrscanner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.ActivityMainBinding
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.ui.fragments.HistoryFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ExecutionException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _mCamera: Camera? = null
    private val mCamera get() = _mCamera!!
    private var isImageSelected = false
    private var doublePressToExit = false
    private var isClickedAllowButton = false
    private lateinit var binding: ActivityMainBinding
    private val cameraRequestCode = 100
    private var mChoosePhoto: ActivityResultLauncher<String>? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private lateinit var mCameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    private val barcodeViewModel: BarcodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSystemBars()

        binding.expand.setOnClickListener { bottomSheet() }

        mCameraProviderFuture = ProcessCameraProvider.getInstance(this)
        requestCameraPermission()
        binding.selectFromGallery.setOnClickListener { mChoosePhoto!!.launch("image/*") }
        binding.history.setOnClickListener {
            mCameraProvider?.unbindAll()
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack("historyFrag")
                add<HistoryFragment>(R.id.main_layout)
            }
        }
        mChoosePhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
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
        if (isClickedAllowButton) {
            requestCameraPermission()
            isClickedAllowButton = false
        }
        if (binding.buttonFlashOff.visibility == View.GONE) {
            binding.buttonFlashOn.visibility = View.GONE
            binding.buttonFlashOff.visibility = View.VISIBLE
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) setUpCamera()
        else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            cameraRequestCode ->                 // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpCamera()
                } else {
                    val message = resources.getString(R.string.permissionDeniedMessageCam)
                    noPermissionDialog(message)
                }
        }
    }

    private fun setUpCamera() {
        mCameraProviderFuture.addListener({
            try {
                mCameraProvider = mCameraProviderFuture.get()
                processScan()
            } catch (e: ExecutionException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.d("MainActivity", "An error occurred: $e")

            } catch (e: InterruptedException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
                Log.d("MainActivity", "An error occurred: $e")
            }
        }, ContextCompat.getMainExecutor(this))
    }


    private fun noPermissionDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Denied!")
            .setMessage(message)
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setPositiveButton("Allow") { _: DialogInterface?, _: Int ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", this@MainActivity.packageName, null)
                intent.data = uri
                startActivity(intent)
                isClickedAllowButton = true
            }
        val dialog = builder.create()
        dialog.show()
        dialog.makeButtonTextBlue()
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
                    Toast.makeText(this@MainActivity, "Failed to scan.", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
                .addOnCompleteListener(ContextCompat.getMainExecutor(this)) { imageProxy.close() }
        }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        _mCamera = mCameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)

        zoomControl()
        flashControl()
    }

    private fun processResult(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            mCameraProvider?.unbindAll()
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
            val vibrateAllowed = sharedPreferences.getBoolean("vibration_preference", true)
            if (vibrateAllowed) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 125))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
            sendRequiredData(barcodes[0])
            isImageSelected = false
            setUpCamera()
        } else if (isImageSelected) {
            Toast.makeText(this, "Failed to scan", Toast.LENGTH_SHORT).show()
            isImageSelected = false
        }
    }

    /*
    * SSID, title, text, number, phone_number, raw, barcodes => title: String
    *
    * password, url, message => decryptedText: String
    *
    * encryptionType, ($latitude,$longitude) => others: String
    *
    * */

    private fun sendRequiredData(barcode: Barcode){

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
            when(barcode.valueType){
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

                Barcode.TYPE_TEXT -> { title = barcode.displayValue }
                Barcode.TYPE_PHONE -> { title = barcode.phone?.number }

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


        val barcodeData = BarcodeData(format, valueType, title, decryptedText, others, dateTime)

        val saveScan = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("save_scans_preference", true)
        if (saveScan) barcodeViewModel.insert(barcodeData)

        intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("barcodeData", barcodeData)
        startActivity(intent)
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
        binding.buttonFlashOff.setOnClickListener {
            if (mCamera.cameraInfo.hasFlashUnit()) {
                mCamera.cameraControl.enableTorch(true)
                binding.buttonFlashOff.visibility = View.GONE
                binding.buttonFlashOn.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    this, "Sorry, Your phone don't have a flashlight", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonFlashOn.setOnClickListener {
            mCamera.cameraControl.enableTorch(false)
            binding.buttonFlashOn.visibility = View.GONE
            binding.buttonFlashOff.visibility = View.VISIBLE
        }
    }

    private fun bottomSheet() {
        val dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_main)

        val dismissButton = dialog.findViewById<ImageButton>(R.id.dismiss_button)
        val settings = dialog.findViewById<LinearLayout>(R.id.settings_layout)
        val rateUs = dialog.findViewById<LinearLayout>(R.id.rate_layout)
        val shareApp = dialog.findViewById<LinearLayout>(R.id.share_layout)
        val privacyPolicy = dialog.findViewById<LinearLayout>(R.id.policy_layout)

        dismissButton.setOnClickListener {
            dialog.dismiss()
        }
        settings.setOnClickListener {
            dialog.dismiss()
            intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        rateUs.setOnClickListener {
            dialog.dismiss()
            ratingDialog()
        }
        shareApp.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT,
                "Hey!! check out this awesome QR Scanner app at Play Store: https://play.google.com/store/apps/details?id=com.yatik.qrscanner and at Galaxy Store: https://apps.samsung.com/appquery/appDetail.as?appId=com.yatik.qrscanner")
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share app via"))
        }
        privacyPolicy.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://sites.google.com/view/yatik-qr-scanner/home")))
        }

        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()
    }


    @SuppressLint("SetTextI18n")
    private fun ratingDialog() {
        val dialog = Dialog(this@MainActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.rating_layout)

        var currentRating = 4.5F
        val myRatingBar = dialog.findViewById<RatingBar>(R.id.rating_bar)
        val submitButton = dialog.findViewById<Button>(R.id.submit_button)
        val cancelButton = dialog.findViewById<Button>(R.id.cancel_button)
        val feedbackText = dialog.findViewById<TextView>(R.id.rating_feedback_text)

        myRatingBar.setOnRatingBarChangeListener { ratingBar, _, _ ->
            currentRating = ratingBar.rating
            if (currentRating in 0.5..2.0) {
                feedbackText.text = "Very Bad 😠"
            } else if (currentRating in 2.5..3.5) {
                feedbackText.text = "Fair 😏"
            } else if (currentRating.toInt() == 4) {
                feedbackText.text = "Good ✌"
            } else if (currentRating in 4.5..5.0) {
                feedbackText.text = "Excellent 😍💕"
            } else {
                feedbackText.text = "🎶🎶"
            }
        }
        submitButton.setOnClickListener {
            when (currentRating) {
                in 0.5..3.5 -> {
                    dialog.dismiss()
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:yatikapps@outlook.com")
                    startActivity(intent)
                }
                in 4.0..5.0 -> {
                    dialog.dismiss()
                    startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.yatik.qrscanner")))
                }
                else -> {
                    Toast.makeText(this, "Can't rate 0 ⭐", Toast.LENGTH_SHORT).show()
                }
            }
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }


    private fun setSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide the status bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        window.navigationBarColor = getColor(R.color.main_background)

        // Make navigation bar transparent
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
    }


    private fun AlertDialog.makeButtonTextBlue() {
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context,
            R.color.dialogButtons
        ))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context,
            R.color.dialogButtons
        ))
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            setUpCamera()
            supportFragmentManager.popBackStack()
            binding.buttonFlashOn.visibility = View.GONE
            binding.buttonFlashOff.visibility = View.VISIBLE
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