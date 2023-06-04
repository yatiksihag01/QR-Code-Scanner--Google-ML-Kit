package com.yatik.qrscanner.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.yatik.qrscanner.R
import com.yatik.qrscanner.utils.Utilities.Companion.makeButtonTextTeal

@SuppressLint("SetTextI18n")
fun ratingDialog(context: Context) {
    val dialog = Dialog(context)
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
                intent.data = Uri.parse(Constants.SUPPORT_MAIL)
                context.startActivity(intent)
            }

            in 4.0..5.0 -> {
                dialog.dismiss()
                val installer: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } else {
                    @Suppress("Deprecation")
                    context.packageManager.getInstallerPackageName("")
                }
                if (installer == "com.sec.android.app.samsungapps")
                    redirectToStore(context, Constants.GALAXY_STORE)
                else redirectToStore(context, Constants.PLAY_STORE)
            }

            else -> {
                Toast.makeText(context, "Can't rate 0 ⭐", Toast.LENGTH_SHORT).show()
            }
        }
    }
    cancelButton.setOnClickListener {
        dialog.dismiss()
    }

    dialog.window!!.setLayout(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.show()

}

private fun redirectToStore(context: Context, storeLink: String) {
    context.startActivity(
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(storeLink)
        )
    )
}

fun noPermissionDialog(context: Context, message: String) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Permission Denied!")
        .setMessage(message)
        .setCancelable(false)
        .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        .setPositiveButton("Allow") { _: DialogInterface?, _: Int ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    val dialog = builder.create()
    dialog.window?.setBackgroundDrawable(
        ContextCompat.getDrawable(context, R.drawable.dialog_background)
    )
    dialog.show()
    dialog.makeButtonTextTeal(context)
}
