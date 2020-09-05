package com.example.adrian.datesgmb.ipc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.adrian.datesgmb.databinding.ListItemBinding
import com.example.adrian.datesgmb.network.IPCProperty

/**
 * This class implements a [RecyclerView] [ListAdapter] which uses Data Binding to present [List]
 * data, including computing diffs between lists.
 * @param onClickListener a lambda that takes the listener
 */
class IPCListAdapter(private val onClickListener: OnClickListener ) :
    ListAdapter<IPCProperty, IPCListAdapter.IPCPropertyViewHolder>(DiffCallback) {
    /**
     * The MarsPropertyViewHolder constructor takes the binding variable from the associated
     * GridViewItem, which nicely gives it access to the full [IPCProperty] information.
     */
    class IPCPropertyViewHolder(private var binding: ListItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ipcProperty: IPCProperty) {
            binding.property = ipcProperty
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [IPCProperty]
     * has been updated.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<IPCProperty>() {
        override fun areItemsTheSame(oldItem: IPCProperty, newItem: IPCProperty): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: IPCProperty, newItem: IPCProperty): Boolean {
            return oldItem.date == newItem.date
        }
    }

    /**
     * Create new [RecyclerView] item views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): IPCPropertyViewHolder {
        return IPCPropertyViewHolder(ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: IPCPropertyViewHolder, position: Int) {
        val ipcProperty = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(ipcProperty)
        }
        holder.bind(ipcProperty)
    }

    /**
     * Custom listener that handles clicks on [RecyclerView] items.  Passes the [IPCProperty]
     * associated with the current item to the [onClick] function.
     * @param clickListener lambda that will be called with the current [IPCProperty]
     */
    class OnClickListener(val clickListener: (ipcProperty: IPCProperty) -> Unit) {
        fun onClick(ipcProperty: IPCProperty) = clickListener(ipcProperty)
    }
}