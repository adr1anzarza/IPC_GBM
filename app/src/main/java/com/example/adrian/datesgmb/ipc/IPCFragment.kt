package com.example.adrian.datesgmb.ipc

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.adrian.datesgmb.MainActivity
import com.example.adrian.datesgmb.R
import com.example.adrian.datesgmb.databinding.IpcFragmentBinding

/**
 * This fragment shows the the status of the IPC web services transaction.
 */
class IPCFragment : Fragment() {

    /**
     * Lazily initialize our [IPCViewModel].
     */
    private val viewModel: IPCViewModel by lazy {
        ViewModelProviders.of(this).get(IPCViewModel::class.java)
    }

    /**
     * Inflates the layout with Data Binding, sets its lifecycle owner to the IPCFragment
     * to enable Data Binding to observe LiveData
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = IpcFragmentBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = this

        // Giving the binding access to the OverviewViewModel
        binding.viewModel = viewModel

        // Sets the adapter of the photosGrid RecyclerView with clickHandler lambda that
        // tells the viewModel when our property is clicked
        binding.pricesList.adapter = IPCListAdapter(IPCListAdapter.OnClickListener {
            Toast.makeText(context, "${it.price}", Toast.LENGTH_SHORT).show()
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    /**
     * Inflates the ipc menu that contains showing options.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Updates the screen in the [IPCViewModel] when the menu items are selected from the
     * ipc menu.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
                R.id.Table -> navigateToTable()
                R.id.Graph -> navigateToGraph()
                else -> super.onOptionsItemSelected(item)
            }
        return true
    }

    private fun navigateToGraph() {

    }

    private fun navigateToTable(){

    }
}