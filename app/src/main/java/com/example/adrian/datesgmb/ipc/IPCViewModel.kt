package com.example.adrian.datesgmb.ipc

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.adrian.datesgmb.network.IPCApi
import com.example.adrian.datesgmb.network.IPCProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class IPCViewModel: ViewModel() {

    // Internally, we use a MutableLiveData, because we will be updating the List of MarsProperty
    // with new values
    private val _properties = MutableLiveData<List<IPCProperty>>()

    // The external LiveData interface to the property is immutable, so only this class can modify
    val properties: LiveData<List<IPCProperty>>
        get() = _properties

    private val _dates = MutableLiveData<List<Date>>()

    val dates: LiveData<List<Date>>
        get() = _dates

    // Create a Coroutine scope using a job to be able to cancel when needed
    private var viewModelJob = Job()

    // the Coroutine runs using the Main (UI) dispatcher
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    init{
        getIPCDatesProperties()

    }



    private fun getIPCDatesProperties() {
        coroutineScope.launch {
            // Get the Deferred object for our Retrofit request
            var getPropertiesDeferred = IPCApi.retrofitService.getDates()
            try {
                val listResult = getPropertiesDeferred.await()
                _properties.value = listResult

            } catch (e: Exception) {
                _properties.value = ArrayList()
            }
        }
    }

    private fun getDates(size: Int) : MutableList<Date>{
        var dates: MutableList<Date> = mutableListOf<Date>()
        for (ipcProperty in _properties.value!!){
            var format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            try {
                val date = format.parse(ipcProperty.date)
                dates.add(date)
                Log.i("DATE: ", date.toString())
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }
        return dates
    }
}