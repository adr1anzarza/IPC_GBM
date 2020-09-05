package com.example.adrian.datesgmb.network

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IPCProperty(
    val date: String,
    val price: Double,
    val percentageChange: Double,
    val volume: Long,
    val change: Double) : Parcelable
