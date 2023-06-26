package com.yatik.qrscanner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yatik.qrscanner.R

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

class PermissionHelper {

    fun isCameraPermissionGranted(context: Context): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * Checks if the camera permission is already granted,
     * and if not, it launches a permission request dialog using the [ActivityResultContracts.RequestPermission] API.
     * It also handles the result of the permission request and shows a [noPermissionDialog] if the permission is denied.
     *
     * @param context Context object of the registering fragment.
     * @param fragment The fragment object that is registering for the permission request result.
     *
     * @see [Manifest.permission.CAMERA]
     */
    fun requestCameraPermission(context: Context, fragment: Fragment) {

        val requestPermissionLauncher: ActivityResultLauncher<String>

        if (!isCameraPermissionGranted(context)) {
            requestPermissionLauncher = fragment.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                if (!it) {
                    noPermissionDialog(
                        context,
                        context.getString(R.string.permissionDeniedMessageCam)
                    )
                }
            }
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

}