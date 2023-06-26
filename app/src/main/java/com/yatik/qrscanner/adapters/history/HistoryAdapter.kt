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
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.R
import com.yatik.qrscanner.models.BarcodeData

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

class HistoryAdapter :
    PagingDataAdapter<BarcodeData, HistoryAdapter.BarcodeDataViewHolder>(DiffCallback) {

    inner class BarcodeDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    object DiffCallback : DiffUtil.ItemCallback<BarcodeData>() {
        override fun areItemsTheSame(oldItem: BarcodeData, newItem: BarcodeData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BarcodeData, newItem: BarcodeData): Boolean {
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

        val barcodeData = getItem(position)
        val itemView = holder.itemView

        val icon: ImageView = itemView.findViewById(R.id.history_icon)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_button)
        val typeText: TextView = itemView.findViewById(R.id.type_text_history)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val date: TextView = itemView.findViewById(R.id.dateTime)

        val format = barcodeData?.format
        val type = barcodeData?.type
        val title = barcodeData?.title
        val dateTime = barcodeData?.dateTime

        date.text = "· $dateTime"

        tvTitle.text = barcodeData?.title?.let {
            if (it.isEmpty()) {
                val text = barcodeData.decryptedText.toString()
                if (text.length <= 50) text
                else "${text.substring(0, 47)}..."
            } else if (it.length <= 50) it
            else "${it.substring(0, 47)}..."
        }
        when (format) {
            Barcode.FORMAT_QR_CODE -> {
                when (type) {
                    Barcode.TYPE_TEXT -> {
                        if (title!!.startsWith("upi://pay")) {
                            typeText.setText(R.string.upi)
                            icon.setImageResource(R.drawable.upi_24)
                        } else {
                            typeText.setText(R.string.text)
                            icon.setImageResource(R.drawable.outline_text_icon)
                        }
                    }

                    Barcode.TYPE_URL -> {
                        typeText.setText(R.string.url)
                        icon.setImageResource(R.drawable.outline_url_24)
                    }

                    Barcode.TYPE_PHONE -> {
                        typeText.setText(R.string.phone)
                        icon.setImageResource(R.drawable.outline_call_24)
                    }

                    Barcode.TYPE_SMS -> {
                        typeText.setText(R.string.sms)
                        icon.setImageResource(R.drawable.outline_sms_24)
                    }

                    Barcode.TYPE_WIFI -> {
                        typeText.setText(R.string.wifi)
                        icon.setImageResource(R.drawable.outline_wifi_24)
                    }

                    Barcode.TYPE_GEO -> {
                        typeText.setText(R.string.location)
                        icon.setImageResource(R.drawable.outline_location_24)
                    }

                    else -> {
                        typeText.setText(R.string.raw)
                        icon.setImageResource(R.drawable.outline_question_mark_24)
                    }
                }
            }

            Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E, Barcode.FORMAT_EAN_8, Barcode.FORMAT_EAN_13, Barcode.TYPE_ISBN -> {
                typeText.setText(R.string.product)
                icon.setImageResource(R.drawable.outline_product_24)
            }

            else -> {
                typeText.setText(R.string.barcode)
                icon.setImageResource(R.drawable.outline_barcode_24)
            }
        }
        itemView.setOnClickListener {
            onItemClickListener?.let {
                if (barcodeData != null) {
                    it(barcodeData)
                }
            }
        }
        deleteButton.setOnClickListener {
            onDeleteClickListener?.let {
                if (barcodeData != null)
                    it(barcodeData)
            }
        }
    }

    private var onItemClickListener: ((BarcodeData) -> Unit)? = null
    private var onDeleteClickListener: ((BarcodeData) -> Unit)? = null

    fun setOnItemClickListener(listener: (BarcodeData) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnDeleteClickListener(listener: (BarcodeData) -> Unit) {
        onDeleteClickListener = listener
    }

    fun getItemOnPosition(position: Int): BarcodeData {
        return getItem(position)!!
    }

}