package com.example.adrian.datesgmb.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.adrian.datesgmb.ipc.IPCListAdapter
import com.example.adrian.datesgmb.network.IPCProperty

/**
 * When there is no Mars property data (data is null), hide the [RecyclerView], otherwise show it.
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<IPCProperty>?) {
    val adapter = recyclerView.adapter as IPCListAdapter
    adapter.submitList(data)
}