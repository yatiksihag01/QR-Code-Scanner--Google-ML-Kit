package com.yatik.qrscanner.ui

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.yatik.qrscanner.R

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        window.navigationBarColor = getColor(R.color.main_background)

        val settingsToolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        settingsToolbar.setNavigationOnClickListener {
            finish()
            startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
        }
    }

    class QRScannerPreferenceFragment : PreferenceFragment() {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Log.d("QRScannerPreferenceFragment", "Loading preferences")
            addPreferencesFromResource(R.xml.preferences)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(
            Intent(this@SettingsActivity, MainActivity::class.java)
        )
    }

}
