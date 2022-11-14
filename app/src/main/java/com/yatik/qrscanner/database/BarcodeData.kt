package com.yatik.qrscanner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
* SSID, title, number, phone_number, raw => title: String
*
* password, url, message => decryptedText: String
*
* encryptionType, ($latitude,$longitude) => others: String
*
* */

@Entity(tableName = "barcode_table")
class BarcodeData(@ColumnInfo(name = "type") val type: Int,
                  @ColumnInfo(name = "title") val title: String?,
                  @ColumnInfo(name = "decryptedText") val decryptedText: String?,
                  @ColumnInfo(name = "others") val others: String?,
                  @ColumnInfo(name = "dateTime") val dateTime: String) {

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id = 0

}