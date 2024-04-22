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

package com.yatik.qrscanner.ui.fragments.details

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_WIFI_ADD_NETWORKS
import android.provider.Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED
import android.provider.Settings.ADD_WIFI_RESULT_ALREADY_EXISTS
import android.provider.Settings.ADD_WIFI_RESULT_SUCCESS
import android.provider.Settings.EXTRA_WIFI_NETWORK_LIST
import android.provider.Settings.EXTRA_WIFI_NETWORK_RESULT_LIST
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.adapters.food.FoodTableAdapter
import com.yatik.qrscanner.databinding.FragmentDetailsBinding
import com.yatik.qrscanner.models.BarcodeData
import com.yatik.qrscanner.models.food.Product
import com.yatik.qrscanner.ui.MainActivity
import com.yatik.qrscanner.utils.Resource
import com.yatik.qrscanner.utils.Utilities
import com.yatik.qrscanner.utils.Utilities.Companion.makeButtonTextTeal
import com.yatik.qrscanner.utils.afterWiFiSavingDialog
import com.yatik.qrscanner.utils.foodTableRowsList
import com.yatik.qrscanner.utils.getNovaInfo
import com.yatik.qrscanner.utils.getNutriInfo
import com.yatik.qrscanner.utils.isBookBarcode
import com.yatik.qrscanner.utils.mappers.barcodeDataToGeneratorData
import com.yatik.qrscanner.utils.setNovaColor
import com.yatik.qrscanner.utils.setNutriColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: DetailsFragmentArgs by navArgs()
    private val detailsViewModel: DetailsViewModel by viewModels()
    private lateinit var wifiSettingsLauncher: ActivityResultLauncher<Intent>
    private var isClickedGetDetails = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val barcodeData = args.barcodeData
        getBarcodeDetails(barcodeData)

        binding.detailsToolbar.setNavigationOnClickListener {
            requireActivity().finish()
            requireActivity().intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(requireActivity().intent)
        }

        wifiSettingsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                handleWiFiResultResponse(result, barcodeData.title!!)
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isClickedGetDetails", isClickedGetDetails)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        isClickedGetDetails = savedInstanceState?.getBoolean("isClickedGetDetails") ?: false
        if (isClickedGetDetails)
            showNutriments(args.barcodeData.title.toString())
    }

    @SuppressLint("SetTextI18n")
    private fun getBarcodeDetails(barcodeData: BarcodeData) {

        val format = barcodeData.format
        val valueType = barcodeData.type
        val title = barcodeData.title
        val decryptedText = barcodeData.decryptedText
        val others = barcodeData.others

        val rawValue = title ?: getString(R.string.empty_qr_message)

        binding.detailsToQrButton.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("GeneratorData", barcodeDataToGeneratorData(barcodeData))
            }
            findNavController().navigate(
                R.id.action_detailsFragment_to_generatorFragment,
                bundle
            )
        }

        when (format) {
            Barcode.FORMAT_QR_CODE -> {
                binding.detailsToolbar.menu.removeItem(R.id.get_food_details_button)
                when (valueType) {
                    Barcode.TYPE_WIFI -> {
                        binding.typeIcon.setImageResource(R.drawable.outline_wifi_24)
                        binding.typeText.text = getString(R.string.wifi)
                        binding.decodedText.text = String.format(
                            "SSID: %s\n\nPassword: %s\n\nType: %s", title, decryptedText, others
                        )
                        binding.launchButton.icon = AppCompatResources.getDrawable(
                            requireContext(),
                            R.drawable.outline_wifi_32
                        )
                        binding.launchButton.text = getString(R.string.wifi)
                        binding.launchButton.setOnClickListener {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                openWifiSettings(title!!, others!!, decryptedText)
                            } else {
                                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            }
                        }
                    }

                    Barcode.TYPE_URL -> {
                        if (!decryptedText.isNullOrBlank())
                            setUrlView(decryptedText)
                    }

                    Barcode.TYPE_TEXT -> {
                        binding.decodedText.text = title
                        if (title!!.startsWith("upi://pay")) {
                            binding.typeIcon.setImageResource(R.drawable.upi_24)
                            binding.typeText.text = getString(R.string.upi)
                            binding.launchButton.setOnClickListener { payViaUPI(title) }
                        } else {
                            binding.typeIcon.setImageResource(R.drawable.outline_text_icon)
                            binding.typeText.setText(R.string.text)
                            binding.launchButton.setOnClickListener { shareData(title) }
                        }
                    }

                    Barcode.TYPE_PHONE -> {
                        binding.typeIcon.setImageResource(R.drawable.outline_call_24)
                        binding.typeText.setText(R.string.phone)
                        binding.decodedText.text = String.format("Phone. No: %s", title)
                        binding.launchButton.setOnClickListener {
                            startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", title, null)
                                )
                            )
                        }
                    }

                    Barcode.TYPE_GEO -> {
                        val longLat = others?.split(",")
                        val latitude = longLat?.get(0)
                        val longitude = longLat?.get(1)
                        binding.typeIcon.setImageResource(R.drawable.outline_location_24)
                        binding.typeText.text = getString(R.string.location)
                        binding.decodedText.text =
                            String.format("Latitude: %s\n\nLongitude: %s", latitude, longitude)
                        binding.launchButton.setOnClickListener {
                            val gmmIntentUri =
                                Uri.parse(String.format("geo:%s,%s", latitude, longitude))
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            if (requireActivity().packageManager != null) {
                                startActivity(mapIntent)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.maps_not_installed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    Barcode.TYPE_SMS -> {
                        binding.typeIcon.setImageResource(R.drawable.outline_sms_24)
                        binding.typeText.setText(R.string.sms)
                        binding.decodedText.text = String.format(
                            "Phone. No: %s\n\nMessage: %s", title, decryptedText
                        )
                        binding.launchButton.setOnClickListener {
                            shareData(decryptedText ?: "")
                        }
                    }

                    else -> {
                        binding.typeIcon.setImageResource(R.drawable.outline_question_mark_24)
                        binding.typeText.setText(R.string.raw)
                        binding.decodedText.text = rawValue
                        binding.launchButton.setOnClickListener {
                            shareData(rawValue)
                        }
                    }
                }
            }

            Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E, Barcode.FORMAT_EAN_8, Barcode.FORMAT_EAN_13, Barcode.TYPE_ISBN -> {
                setProductView(barcodeData)

            }

            else -> {
                binding.detailsToolbar.menu.removeItem(R.id.get_food_details_button)
                binding.typeIcon.setImageResource(R.drawable.outline_barcode_24)
                binding.typeText.text = getString(R.string.barcode)
                binding.decodedText.text = title
                binding.launchButton.setOnClickListener {
                    shareData(rawValue)
                }
            }
        }
        binding.copyButton.setOnClickListener {
            val text = binding.decodedText.text.toString()
            copyData(text)
        }
        binding.shareButton.setOnClickListener {
            shareData(
                binding.decodedText.text.toString()
            )
        }
    }

    private fun setUrlView(url: String) {

        val utilities = Utilities()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        binding.typeIcon.setImageResource(R.drawable.outline_url_24)
        binding.typeText.setText(R.string.url)
        val mainUrl: String = if (!url.startsWith("http")) "https://$url"
        else url
        binding.decodedText.text = mainUrl

        val shimmerContainer = binding.shimmerViewContainer
        shimmerContainer.apply {
            visibility = View.VISIBLE
            startShimmer()
        }
        binding.urlTextView.text =
            if (mainUrl.length <= 50) mainUrl
            else "${mainUrl.substring(0, 47)}..."
        binding.urlTextView.setOnClickListener {
            utilities.customTabBuilder(requireContext(), Uri.parse(mainUrl))
        }
        binding.urlTextView.setOnLongClickListener {
            copyData(mainUrl)
            true
        }

        detailsViewModel.getUrlPreview(mainUrl)
        detailsViewModel.urlPreviewResource.observe(viewLifecycleOwner) { resource ->

            if (!resource.data?.title.isNullOrBlank()) {
                shimmerContainer.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
                binding.previewDetails.visibility = View.VISIBLE
                binding.urlContent.text = resource.data?.title
            } else if (!resource.message.isNullOrBlank()) {
                shimmerContainer.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
                Toast.makeText(
                    requireContext(),
                    "${resource.message}! Preview not available",
                    Toast.LENGTH_SHORT
                ).show()
                binding.urlContent.text = ""
            }
            if (!resource.data?.imageUrl.isNullOrBlank())
                binding.previewImage.load(resource.data?.imageUrl) {
                    crossfade(true)
                    scale(Scale.FILL)
                    placeholder(R.drawable.broken_image_200)
                    transformations(RoundedCornersTransformation(10.0F))
                }
            else binding.previewImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.broken_image_200
                )
            )

        }
        binding.launchButton.setOnClickListener {
            utilities.customTabBuilder(requireContext(), Uri.parse(mainUrl))
        }
        val openAutomatically =
            sharedPreferences.getBoolean("open_url_preference", false)
        if (openAutomatically) {
            utilities.customTabBuilder(requireContext(), Uri.parse(mainUrl))
        }
    }

    private fun setProductView(barcodeData: BarcodeData) {

        val barcode = barcodeData.title
        binding.typeIcon.setImageResource(R.drawable.outline_product_24)
        binding.typeText.text = getString(R.string.product)
        binding.decodedText.text = barcode
        binding.launchButton.setOnClickListener {
            Utilities().customTabBuilder(
                requireContext(),
                Uri.parse("https://www.google.com/search?q=$barcode")
            )
        }
        if (!barcode.isNullOrBlank() && isBookBarcode(barcode))
            binding.detailsToolbar.menu.removeItem(R.id.get_food_details_button)
        else {
            binding.detailsToolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.get_food_details_button -> {
                        showNutrimentsDialog(barcode.toString())
                        true
                    }

                    else -> false
                }
            }
        }

    }

    private fun copyData(text: String) {
        val clipboardManager =
            requireActivity().getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("copied", text)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareData(text: String) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, text)
        sendIntent.type = "text/plain"
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun payViaUPI(ID: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ID))
        val payIntent = Intent.createChooser(intent, "Pay with:")
        startActivity(payIntent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun handleWiFiResultResponse(result: ActivityResult, ssid: String) {
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null && data.hasExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)) {
                for (code in data.getIntegerArrayListExtra(EXTRA_WIFI_NETWORK_RESULT_LIST)!!) {
                    when (code) {
                        ADD_WIFI_RESULT_SUCCESS -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.wifi_config_saved,
                                Toast.LENGTH_LONG
                            ).show()
                            afterWiFiSavingDialog(requireContext(), ssid)
                        }

                        ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.invalid_wifi_config,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        ADD_WIFI_RESULT_ALREADY_EXISTS -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.wifi_config_exist,
                                Toast.LENGTH_LONG
                            ).show()
                            afterWiFiSavingDialog(requireContext(), ssid)
                        }

                        else -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.something_went_wrong,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun openWifiSettings(ssid: String, securityType: String, password: String?) {
        val bundle = Bundle()
        bundle.putParcelableArrayList(
            EXTRA_WIFI_NETWORK_LIST,
            detailsViewModel.getWiFiSuggestionsList(ssid, securityType, password)
        )
        val intent = Intent(ACTION_WIFI_ADD_NETWORKS)
        intent.putExtras(bundle)
        wifiSettingsLauncher.launch(intent)
    }

    private fun showNutriments(barcode: String) {
        binding.detailsToolbar.menu.removeItem(R.id.get_food_details_button)
        val shimmerContainer = binding.shimmerFoodViewContainer
        shimmerContainer.visibility = View.VISIBLE
        shimmerContainer.startShimmer()

        detailsViewModel.getFoodDetails(barcode)
        detailsViewModel.foodProductResource.observe(viewLifecycleOwner) { resource ->
            val data = resource.data
            when (resource) {
                is Resource.Loading ->
                    if (data != null) {
                        shimmerContainer.stopShimmer()
                        shimmerContainer.visibility = View.GONE
                        attachProductData(data)
                    }

                is Resource.Success -> {
                    shimmerContainer.stopShimmer()
                    shimmerContainer.visibility = View.GONE
                    data?.let { attachProductData(it) }
                }

                else -> {
                    shimmerContainer.stopShimmer()
                    shimmerContainer.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showNutrimentsDialog(barcode: String) {
        val builder = AlertDialog.Builder(requireContext()).apply {

            setTitle(getString(R.string.food_product_get_nutriments))
            setMessage(getString(R.string.get_nutriments_message) + "\n")
            setNegativeButton(context.getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }

            setPositiveButton(getString(R.string.get_details)) { _: DialogInterface?, _: Int ->
                isClickedGetDetails = true
                showNutriments(barcode)
            }
        }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background)
        )
        dialog.show()
        dialog.makeButtonTextTeal(requireContext())
    }

    private fun attachProductData(product: Product) {

        val foodDetailsParent = binding.foodDetailsParentLayout
        val foodDetails = binding.foodDetails

        foodDetailsParent.visibility = View.VISIBLE
        setProductPreviewImage(product)

        foodDetails.novaRatingInfo.text = getNovaInfo(
            requireContext(),
            product.nutriments?.novaGroup
        )
        foodDetails.nutriScoreInfo.text = getNutriInfo(
            requireContext(),
            product.nutriscoreData?.grade
        )

        setNovaColor(
            requireContext(),
            foodDetails.novaMainRatingTv,
            product.nutriments?.novaGroup
        )
        setNutriColor(
            requireContext(),
            foodDetails.nutriScoreRatingTv,
            product.nutriscoreData?.grade
        )

        val recyclerView = binding.foodDetails.foodTableRecyclerView
        val adapter = FoodTableAdapter(foodTableRowsList(product))
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun setProductPreviewImage(product: Product) {
        binding.foodDetails.productFrontImg.load(product.frontImageSmall) {
            crossfade(true)
            scale(Scale.FILL)
            placeholder(R.drawable.broken_image_200)
            transformations(RoundedCornersTransformation(10.0F))
        }
        binding.foodDetails.productName.text = product.productName
        binding.foodDetails.productBrandAndServingQuantity.text =
            "${product.brands} · ${product.quantity}"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
