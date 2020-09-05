package com.example.adrian.datesgmb.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL = "https://run.mocky.io/v3/"
enum class MarsApiFilter(val value: String) { SHOW_RENT("rent"), SHOW_BUY("buy"), SHOW_ALL("all") }

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

/**
 * Use the Retrofit builder to build a retrofit object using a Moshi converter with our Moshi
 * object.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

/**
 * A public interface that exposes the [getDates] method
 */
interface IPCApiService {
    /**
     * Returns a Coroutine [Deferred] [List] of [IPCProperty] which can be fetched with await() if
     * in a Coroutine scope.
     * The @GET annotation indicates that the "cc4c350b-1f11-42a0-a1aa-f8593eafeb1e" endpoint will be requested with the GET
     * HTTP method
     */
    @GET("cc4c350b-1f11-42a0-a1aa-f8593eafeb1e")
    fun getDates(): Deferred<List<IPCProperty>>
}

/**
 * A public Api object that exposes the lazy-initialized Retrofit service
 */
object IPCApi {
    val retrofitService : IPCApiService by lazy { retrofit.create(IPCApiService::class.java) }
}