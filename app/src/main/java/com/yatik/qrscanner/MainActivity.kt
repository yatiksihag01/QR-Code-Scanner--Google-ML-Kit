package com.yatik.qrscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.yatik.qrscanner.databinding.ActivityMainBinding
import com.yatik.qrscanner.fragments.HistoryFragment
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
    private var _mCamera: Camera? = null
    private val mCamera get() = _mCamera
    private var isImageSelected = false
    private var doublePressToExit = false
    private var isClickedAllowButton = false
    private lateinit var binding: ActivityMainBinding
    private val cameraRequestCode = 100
    private var mChoosePhoto: ActivityResultLauncher<String>? = null
    private var mCameraProvider: ProcessCameraProvider? = null
    private lateinit var mCameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars()
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
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
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
        ) {
            setUpCamera()
        } else {
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
            } catch (_: InterruptedException) {
                // No errors need to be handled for this Future.
                // This should never be reached.
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

    private fun AlertDialog.makeButtonTextBlue() {
        this.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
        this.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.dialogButtons))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processScan() {
        val preview = Preview.Builder()
            .build()
        val imageAnalysis = ImageAnalysis.Builder()
            .build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy: ImageProxy ->

            val image = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
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
        flashControl()
    }

    private fun processResult(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(100, 125))
            } else {
                vibrator.vibrate(100)
            }
            mCameraProvider?.unbindAll()
            sendRequiredData(barcodes[0])

            isImageSelected = false
            setUpCamera()
        } else if (isImageSelected) {
            Toast.makeText(this, "Failed to scan", Toast.LENGTH_SHORT).show()
            isImageSelected = false
        }
    }

    /*
    * SSID, title, text, number, phone_number, raw => title: String
    *
    * password, url, message => decryptedText: String
    *
    * encryptionType, ($latitude,$longitude) => others: String
    *
    * */

    private fun sendRequiredData(barcode: Barcode){
        intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("valueType", barcode.valueType)
        when(barcode.valueType){
            Barcode.TYPE_WIFI -> {
                val ssid = barcode.wifi?.ssid
                val password = barcode.wifi?.password
                val encryptionType: String = when (barcode.wifi?.encryptionType) {
                    Barcode.WiFi.TYPE_OPEN -> "Open"
                    Barcode.WiFi.TYPE_WPA -> "WPA"
                    Barcode.WiFi.TYPE_WEP -> "WEP"
                    else -> ""
                }
                intent.putExtra("title", ssid)
                intent.putExtra("decryptedText", password)
                intent.putExtra("others", encryptionType)
            }

            Barcode.TYPE_URL -> {
                val title = barcode.url?.title
                val url = barcode.url?.url
                intent.putExtra("title", title)
                intent.putExtra("decryptedText", url)

            }

            Barcode.TYPE_TEXT -> {
                val text = barcode.displayValue
                intent.putExtra("title", text)
            }

            Barcode.TYPE_PHONE -> {
                val tel = barcode.phone?.number
                intent.putExtra("title", tel)
            }

            Barcode.TYPE_GEO -> {
                val latitude = barcode.geoPoint!!.lat
                val longitude = barcode.geoPoint!!.lng
                val latLongString = "$latitude,$longitude"
                intent.putExtra("others", latLongString)
            }

            Barcode.TYPE_SMS -> {
                val phoneNumber = barcode.sms?.phoneNumber
                val message = barcode.sms?.message
                intent.putExtra("title", phoneNumber)
                intent.putExtra("decryptedText", message)
            }

            else -> {
                val rawValue = barcode.rawValue ?: "Sorry, this QR code doesn't contain any data"
                intent.putExtra("title", rawValue)
            }

        }
        startActivity(intent)
    }

    private fun flashControl() {
        binding.buttonFlashOff.setOnClickListener {
            if (mCamera!!.cameraInfo.hasFlashUnit()) {
                mCamera!!.cameraControl.enableTorch(true)
                binding.buttonFlashOff.visibility = View.GONE
                binding.buttonFlashOn.visibility = View.VISIBLE
            } else {
                Toast.makeText(
                    this, "Sorry, Your phone don't have a flashlight", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonFlashOn.setOnClickListener {
            mCamera!!.cameraControl.enableTorch(false)
            binding.buttonFlashOn.visibility = View.GONE
            binding.buttonFlashOff.visibility = View.VISIBLE
        }
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide the status bar
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        // Make navigation bar transparent
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            binding.buttonFlashOn.visibility = View.GONE
            binding.buttonFlashOff.visibility = View.VISIBLE
            setUpCamera()
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