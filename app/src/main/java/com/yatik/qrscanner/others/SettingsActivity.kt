package com.yatik.qrscanner.others

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.yatik.qrscanner.MainActivity
import com.yatik.qrscanner.R


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        window.navigationBarColor = getColor(R.color.fragButtons)

        val settingsToolbar = findViewById<MaterialToolbar>(R.id.settingsToolbar)
        settingsToolbar.setNavigationOnClickListener {
            startActivity(Intent(this@SettingsActivity, MainActivity::class.java))
        }
    }


    class QRScannerPreferenceFragment : PreferenceFragment() {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}