package com.yatik.qrscanner.ui.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.databinding.FragmentQrCodeGeneratorBinding
import com.yatik.qrscanner.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QRCodeGeneratorFragment : Fragment() {

    private var _binding: FragmentQrCodeGeneratorBinding? = null
    private val binding get() = _binding!!
    private val generatorFragment = GeneratorFragment()

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
            dialog.setContentView(R.layout.text_input_dialog_layout)

            val submitButton = dialog.findViewById<MaterialButton>(R.id.generate_qr)
            val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_generation)
            submitButton.setOnClickListener {
                generatorFragment.type = Barcode.TYPE_TEXT
                generatorFragment.text = "abcdef"

                requireActivity().supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(GeneratorFragment.TAG)
                    setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                    add<GeneratorFragment>(R.id.main_layout)
                }
                dialog.dismiss()
            }
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            showDialog(dialog)
        }
        binding.wifiQrButton.setOnClickListener {
            generatorFragment.type = Barcode.TYPE_WIFI
            generatorFragment.ssid = "Yatik"
            generatorFragment.securityType = "WPA"
            generatorFragment.password = "1801515300"
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack(GeneratorFragment.TAG)
                setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                add<GeneratorFragment>(R.id.main_layout)
            }
        }
        binding.urlQrButton.setOnClickListener {
            generatorFragment.type = Barcode.FORMAT_EAN_13
            generatorFragment.barcodeNumber = "8904054100032"
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                addToBackStack(GeneratorFragment.TAG)
                setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom)
                add<GeneratorFragment>(R.id.main_layout)
            }
        }
        binding.smsQrButton.setOnClickListener {

        }
        binding.phoneQrButton.setOnClickListener {

        }
        binding.ean13BarcodeButton.setOnClickListener {

        }

    }


    private fun showDialog(dialog: Dialog) {
        dialog.window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}