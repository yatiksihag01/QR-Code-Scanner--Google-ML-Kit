package com.yatik.qrscanner.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yatik.qrscanner.R
import com.yatik.qrscanner.utils.ThemeManager.Companion.updateTheme
import com.yatik.qrscanner.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTheme(this@MainActivity)
        setContentView(R.layout.activity_main)
        Utilities().setSystemBars(window, this, true)
    }

}
