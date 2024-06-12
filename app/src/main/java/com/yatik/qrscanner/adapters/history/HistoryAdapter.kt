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

package com.yatik.qrscanner.adapters.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.yatik.qrscanner.R
import com.yatik.qrscanner.models.barcode.BarcodeDetails
import com.yatik.qrscanner.models.barcode.metadata.Format
import com.yatik.qrscanner.models.barcode.metadata.Type

class HistoryAdapter :
    PagingDataAdapter<BarcodeDetails, HistoryAdapter.BarcodeDataViewHolder>(DiffCallback) {

    inner class BarcodeDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    object DiffCallback : DiffUtil.ItemCallback<BarcodeDetails>() {
        override fun areItemsTheSame(oldItem: BarcodeDetails, newItem: BarcodeDetails): Boolean {
            return oldItem.rawValue == newItem.rawValue &&
                    oldItem.timeStamp == newItem.timeStamp
        }

        override fun areContentsTheSame(oldItem: BarcodeDetails, newItem: BarcodeDetails): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeDataViewHolder {
        return BarcodeDataViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recyclerview_item,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BarcodeDataViewHolder, position: Int) {

        val barcode = getItem(position)
        val itemView = holder.itemView

        val icon: ImageView = itemView.findViewById(R.id.history_icon)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_button)
        val typeText: TextView = itemView.findViewById(R.id.type_text_history)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val date: TextView = itemView.findViewById(R.id.dateTime)

        val format = barcode?.format
        val type = barcode?.type
        val dateTime = barcode?.timeStamp

        date.text = "· $dateTime"


        when (format) {
            Format.QR_CODE -> {
                when (type) {
                    Type.TYPE_TEXT -> {
                        if (barcode.text?.startsWith("upi://pay") == true) {
                            typeText.setText(R.string.upi)
                            icon.setImageResource(R.drawable.upi_24)
                        } else {
                            typeText.setText(R.string.text)
                            icon.setImageResource(R.drawable.outline_text_icon)
                        }
                        setTitle(tvTitle, barcode.text)
                    }

                    Type.TYPE_URL -> {
                        typeText.setText(R.string.url)
                        setTitle(tvTitle, barcode.url?.url)
                        icon.setImageResource(R.drawable.outline_url_24)
                    }

                    Type.TYPE_PHONE -> {
                        typeText.setText(R.string.phone)
                        setTitle(tvTitle, barcode.phone?.number)
                        icon.setImageResource(R.drawable.outline_call_24)
                    }

                    Type.TYPE_SMS -> {
                        typeText.setText(R.string.sms)
                        setTitle(tvTitle, barcode.sms?.message)
                        icon.setImageResource(R.drawable.outline_sms_24)
                    }

                    Type.TYPE_WIFI -> {
                        typeText.setText(R.string.wifi)
                        setTitle(tvTitle, barcode.wiFi?.ssid)
                        icon.setImageResource(R.drawable.outline_wifi_24)
                    }

                    Type.TYPE_GEO -> {
                        typeText.setText(R.string.location)
                        setTitle(
                            tvTitle,
                            barcode.geo?.longitude.toString() + ", " + barcode.geo?.latitude
                        )
                        icon.setImageResource(R.drawable.outline_location_24)
                    }

                    Type.TYPE_EMAIL -> {
                        typeText.setText(R.string.email)
                        setTitle(tvTitle, barcode.email?.email)
                        icon.setImageResource(R.drawable.outline_sms_24)
                    }

                    Type.TYPE_ISBN -> {
                        typeText.setText(R.string.product)
                        setTitle(tvTitle, barcode.isbn)
                        icon.setImageResource(R.drawable.outline_product_24)
                    }

                    else -> {
                        typeText.setText(R.string.raw)
                        setTitle(tvTitle, barcode.rawValue)
                        icon.setImageResource(R.drawable.outline_question_mark_24)
                    }
                }
            }

            Format.UPC_A, Format.UPC_E, Format.EAN_13, Format.EAN_8 -> {
                typeText.setText(R.string.product)
                setTitle(tvTitle, barcode.rawValue)
                icon.setImageResource(R.drawable.outline_product_24)
            }

            else -> {
                typeText.setText(R.string.barcode)
                setTitle(tvTitle, barcode?.rawValue)
                icon.setImageResource(R.drawable.outline_barcode_24)
            }
        }
        itemView.setOnClickListener {
            onItemClickListener?.let {
                if (barcode != null) {
                    it(barcode)
                }
            }
        }
        deleteButton.setOnClickListener {
            onDeleteClickListener?.let {
                if (barcode != null)
                    it(barcode)
            }
        }
    }

    private var onItemClickListener: ((BarcodeDetails) -> Unit)? = null
    private var onDeleteClickListener: ((BarcodeDetails) -> Unit)? = null

    fun setOnItemClickListener(listener: (BarcodeDetails) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (BarcodeDetails) -> Unit) {
        onDeleteClickListener = listener
    }

    fun getItemOnPosition(position: Int): BarcodeDetails {
        return getItem(position)!!
    }

    private fun setTitle(textView: TextView, title: String?) {
        textView.text = when {
            title.isNullOrBlank() -> ""
            title.length <= 50 -> title
            else -> title.take(47) + "..."
        }
    }

}