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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarcodeDataViewHolder {
        return BarcodeDataViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: BarcodeDataViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.type)
    }

    class BarcodeDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val icon: ImageView = itemView.findViewById(R.id.history_icon)
        private val typeText: TextView = itemView.findViewById(R.id.type_text)

        fun bind(type: Int) {
            when(type) {
                Barcode.TYPE_TEXT -> typeText.setText(R.string.text)
                Barcode.TYPE_URL -> typeText.setText(R.string.url)
                Barcode.TYPE_PHONE -> typeText.setText(R.string.phone)
                Barcode.TYPE_SMS -> typeText.setText(R.string.sms)
                Barcode.TYPE_WIFI -> typeText.setText(R.string.wifi)
                else -> typeText.setText(R.string.raw)
            }
        }

        companion object {
            fun create(parent: ViewGroup): BarcodeDataViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return BarcodeDataViewHolder(view)
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


}