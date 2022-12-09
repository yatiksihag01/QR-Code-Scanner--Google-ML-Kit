package com.yatik.qrscanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.barcode.common.Barcode
import com.yatik.qrscanner.database.BarcodeData

class BarcodeListAdapter : ListAdapter<BarcodeData, BarcodeListAdapter.BarcodeDataViewHolder>(BarcodeDataComparator()) {

    private lateinit var itemListener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeDataViewHolder {
        return BarcodeDataViewHolder.create(parent, itemListener)
    }

    override fun onBindViewHolder(holder: BarcodeDataViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.format, current.type, current.title, current.dateTime)
    }

    class BarcodeDataViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {

        private val icon: ImageView = itemView.findViewById(R.id.history_icon)
        private val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
        private val typeText: TextView = itemView.findViewById(R.id.type_text_history)
        private val date: TextView = itemView.findViewById(R.id.dateTime)

        fun bind(format: Int, type: Int, title: String?, dateTime: String) {

            deleteButton.setImageResource(R.drawable.outline_delete_24)
            date.text = dateTime
            when(format) {
                Barcode.FORMAT_QR_CODE -> {
                    when(type) {
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
        }

        companion object {
            fun create(parent: ViewGroup, itemListener: OnItemClickListener): BarcodeDataViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return BarcodeDataViewHolder(view, itemListener)
            }
        }

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            deleteButton.setOnClickListener {
                listener.onDeleteClick(adapterPosition)
            }
        }

    }

    class BarcodeDataComparator : DiffUtil.ItemCallback<BarcodeData>() {
        override fun areItemsTheSame(oldItem: BarcodeData, newItem: BarcodeData): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: BarcodeData, newItem: BarcodeData): Boolean {
            return oldItem.id == newItem.id
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {

        itemListener = listener

    }


}