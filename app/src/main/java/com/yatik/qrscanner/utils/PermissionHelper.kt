package com.yatik.qrscanner.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.yatik.qrscanner.R

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