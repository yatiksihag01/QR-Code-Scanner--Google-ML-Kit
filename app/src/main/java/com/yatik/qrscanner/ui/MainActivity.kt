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

package com.yatik.qrscanner.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.yatik.qrscanner.R
import com.yatik.qrscanner.ui.fragments.cropper.CropperFragment
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

        when (intent?.action) {
            Intent.ACTION_SEND -> if (intent.type?.startsWith("image/") == true) {
                val bundle = Bundle()
                @Suppress("DEPRECATION")
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                    bundle.putString(CropperFragment.ARG_KEY, it.toString())
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.main_nav_host_fragment)
                    val navController = navHostFragment?.findNavController()
                    navController?.navigate(
                        R.id.cropperFragment, bundle
                    )
                }
            }
        }
    }

}
