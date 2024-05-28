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

package com.yatik.qrscanner.ui.fragments.generator

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentQrCodeGeneratorBinding
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.data.Phone
import com.yatik.qrscanner.models.barcode.data.Security
import com.yatik.qrscanner.models.barcode.data.Sms
import com.yatik.qrscanner.models.barcode.data.Url
import com.yatik.qrscanner.models.barcode.data.WiFi
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type
import com.yatik.qrscanner.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class QRCodeGeneratorFragment : Fragment() {

    private var _binding: FragmentQrCodeGeneratorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrCodeGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)

        binding.generateQRToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }
        binding.textQrButton.setOnClickListener {
            textFieldDialog(dialog)
        }
        binding.wifiQrButton.setOnClickListener {
            wifiDialog(dialog)
        }
        binding.urlQrButton.setOnClickListener {
            urlDialog(dialog)
        }
        binding.smsQrButton.setOnClickListener {
            smsDialog(dialog)
        }
        binding.phoneQrButton.setOnClickListener {
            phoneDialog(dialog)
        }
        binding.ean13BarcodeButton.setOnClickListener {
            ean13Dialog(dialog)
        }

    }

    private fun textFieldDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.text_input_dialog_layout)
        val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr_text)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation_text)
        val textField = dialog.findViewById<TextInputEditText>(R.id.outlinedTextField)
        submitButton.setOnClickListener {
            val content = textField.text.toString()
            if (content.isBlank()) {
                Toast.makeText(requireContext(), "Please enter valid input", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val barcodeDetails = BarcodeDetails(
                    format = Format.QR_CODE,
                    type = Type.TYPE_TEXT,
                    null, rawValue = content,
                    text = content
                )
                val bundle = Bundle().apply {
                    putParcelable("barcodeDetails", barcodeDetails)
                }
                findNavController().navigate(
                    R.id.action_QRCodeGeneratorFragment_to_generatorFragment,
                    bundle
                )
                dialog.dismiss()
            }
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    private fun wifiDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.wifi_input_dialog_layout)
        val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr_wifi)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation_wifi)
        val ssidField = dialog.findViewById<TextInputEditText>(R.id.outlinedSSIDField)
        val passwordField = dialog.findViewById<TextInputEditText>(R.id.outlinedWifiPasswordField)
        val securityField = dialog.findViewById<AutoCompleteTextView>(R.id.outlinedSecurityField)

        submitButton.setOnClickListener {
            val ssid = ssidField.text.toString()
            val password = passwordField.text.toString()
            val security = when (securityField.text.toString()) {
                "WEP" -> Security.WEP
                "WPA" -> Security.WPA
                "WPA2" -> Security.WPA2
                else -> Security.OPEN
            }
            if (ssid.isBlank()) {
                Toast.makeText(requireContext(), "Please enter SSID", Toast.LENGTH_SHORT).show()
            } else {
                val barcodeDetails = BarcodeDetails(
                    format = Format.QR_CODE,
                    type = Type.TYPE_WIFI,
                    null,
                    "WIFI:S:$ssid;T:${securityField.text};P:$password;",
                    wiFi = WiFi(ssid, password, security)
                )
                val bundle = Bundle().apply {
                    putParcelable("barcodeDetails", barcodeDetails)
                }
                findNavController().navigate(
                    R.id.action_QRCodeGeneratorFragment_to_generatorFragment,
                    bundle
                )
                dialog.dismiss()
            }
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    private fun urlDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.url_input_dialog_layout)
        val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr_url)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation_url)
        val textField = dialog.findViewById<TextInputEditText>(R.id.outlinedUrlField)

        submitButton.setOnClickListener {
            val content = textField.text.toString()

            if (content.startsWith("http://") || content.startsWith("https://")) {
                val barcodeDetails = BarcodeDetails(
                    format = Format.QR_CODE,
                    type = Type.TYPE_URL,
                    null,
                    rawValue = content,
                    url = Url(null, content)
                )
                val bundle = Bundle().apply {
                    putParcelable("barcodeDetails", barcodeDetails)
                }
                findNavController().navigate(
                    R.id.action_QRCodeGeneratorFragment_to_generatorFragment,
                    bundle
                )
                dialog.dismiss()
            } else Toast.makeText(
                requireContext(),
                "Url must start with http:// or https://",
                Toast.LENGTH_SHORT
            ).show()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    private fun smsDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.sms_input_dialog_layout)
        val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr_sms)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation_sms)
        val phoneField = dialog.findViewById<TextInputEditText>(R.id.outlinedSMSPhoneField)
        val messageField = dialog.findViewById<TextInputEditText>(R.id.outlinedMessageField)

        submitButton.setOnClickListener {
            val sms = Sms(phoneField.text.toString(), messageField.text.toString())
            if (sms.number?.isBlank() == true) {
                Toast.makeText(requireContext(), "Please enter phone number", Toast.LENGTH_SHORT)
                    .show()
            } else if (sms.message?.isBlank() == true) {
                Toast.makeText(requireContext(), "Please enter message", Toast.LENGTH_SHORT).show()
            } else {
                val barcodeDetails =
                    BarcodeDetails(
                        format = Format.QR_CODE,
                        type = Type.TYPE_SMS,
                        null,
                        rawValue = "SMSTO:${sms.number};BODY:${sms.message};",
                        sms = sms
                    )
                val bundle = Bundle().apply {
                    putParcelable("barcodeDetails", barcodeDetails)
                }
                findNavController().navigate(
                    R.id.action_QRCodeGeneratorFragment_to_generatorFragment,
                    bundle
                )
                dialog.dismiss()
            }
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    private fun phoneDialog(dialog: Dialog) {

        dialog.setContentView(R.layout.phone_input_dialog_layout)
        val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr_phone)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation_phone)
        val textField = dialog.findViewById<TextInputEditText>(R.id.outlinedPhoneField)

        submitButton.setOnClickListener {
            val content = textField.text.toString()
            if (content.isNotEmpty()) {
                val barcodeDetails = BarcodeDetails(
                    format = Format.QR_CODE,
                    type = Type.TYPE_PHONE,
                    null,
                    rawValue = "tel:$content",
                    phone = Phone(null, content, null)
                )
                val bundle = Bundle().apply {
                    putParcelable("barcodeDetails", barcodeDetails)
                }
                findNavController().navigate(
                    R.id.action_QRCodeGeneratorFragment_to_generatorFragment,
                    bundle
                )
                dialog.dismiss()
            } else Toast.makeText(
                requireContext(),
                "Please enter valid number",
                Toast.LENGTH_SHORT
            ).show()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    private fun ean13Dialog(dialog: Dialog) {

        dialog.setContentView(R.layout.ean13_input_dialog_layout)
        val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr_ean13)
        val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation_ean13)
        val textField = dialog.findViewById<TextInputEditText>(R.id.outlinedEan13Field)

        submitButton.setOnClickListener {
            val content = textField.text.toString()
            if (isValidEAN13(content)) {
                val barcodeDetails =
                    BarcodeDetails(
                        format = Format.EAN_13,
                        type = Type.TYPE_UNKNOWN,
                        timeStamp = null,
                        rawValue = content
                    )
                val bundle = Bundle().apply {
                    putParcelable("barcodeDetails", barcodeDetails)
                }
                findNavController().navigate(
                    R.id.action_QRCodeGeneratorFragment_to_generatorFragment,
                    bundle
                )
                dialog.dismiss()
            } else Toast.makeText(
                requireContext(),
                "Please enter valid input",
                Toast.LENGTH_SHORT
            ).show()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        showDialog(dialog)
    }

    private fun showDialog(dialog: Dialog) {
        dialog.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun isValidEAN13(ean: String): Boolean {
        if (ean.length != 13) return false
        var sum = 0
        for (i in 0 until 12) {
            val digit = ean[i].toString().toInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val checkDigit = (10 - sum % 10) % 10
        return checkDigit == ean[12].toString().toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}