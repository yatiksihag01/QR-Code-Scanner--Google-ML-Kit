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

package com.yatik.qrscanner.ui.cropper

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.canhub.cropper.CropImageView
import com.google.android.material.snackbar.Snackbar
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.ActivityImageCropperBinding
import java.io.File

/**
 * An activity to crop images. Register a request to start the activity for result
 * and launch the activity with Intent containing image URI as a string. The result
 * will contain the cropped image URI.
 *
 * Use [ImageCropperActivity.ITEM_NAME] as the name of image URI String.
 */
class ImageCropperActivity : AppCompatActivity(), CropImageView.OnSetImageUriCompleteListener,
    CropImageView.OnCropImageCompleteListener {

    private lateinit var binding: ActivityImageCropperBinding
    private lateinit var cropImageView: CropImageView
    private lateinit var pickVisualMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var imageUri: Uri? = null

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For better visibility always use dark theme as QR Code is mostly on white background
        this.setTheme(R.style.DarkTheme)
        binding = ActivityImageCropperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = false
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        cropImageView = binding.cropImageView
        cropImageView.setOnSetImageUriCompleteListener(this)
        cropImageView.setOnCropImageCompleteListener(this)

        imageUri = if (savedInstanceState != null) {
            savedInstanceState.getString("imageUri")?.toUri()
        } else intent.getStringExtra(URI_NAME)?.toUri()

        cropImageView.setImageUriAsync(imageUri)
        cropImageView.isShowProgressBar = false

        pickVisualMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result: Uri? ->
                imageUri = result
                cropImageView.setImageUriAsync(imageUri)
            }

        binding.scanCroppedBtn.setOnClickListener { cropImageView.croppedImageAsync() }
        binding.selectAnotherPic.setOnClickListener {
            pickVisualMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
        binding.cropperToolbar.setNavigationOnClickListener { finish() }

        this@ImageCropperActivity.onBackPressedDispatcher.addCallback(
            this@ImageCropperActivity,
            onBackPressedCallback
        )

    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString("imageUri", imageUri.toString())
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

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (!result.isSuccessful) {
            Snackbar.make(
                binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        val uri = result.getUriFilePath(this, uniqueName = false)?.let { File(it).toUri() }
        intent.putExtra(ITEM_NAME, uri)
        setResult(RESULT_CODE_SCAN, intent)
        finish()
    }

    companion object {
        const val RESULT_CODE_SCAN = 726
        const val ITEM_NAME = "cropped_img_uri"
        const val URI_NAME = "image_uri"

    }

}