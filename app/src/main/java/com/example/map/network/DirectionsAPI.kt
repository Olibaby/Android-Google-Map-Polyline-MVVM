package com.example.map.network

import com.example.map.model.response.GetDirectionsResponse
import com.example.map.module.DIRECTIONSAPIBASEURL
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DirectionsAPI {
    @GET("json")
    fun getDirectionsAPI(@Query("origin") origin: String, @Query("destination") destination: String, @Query("key") key: String) : Deferred<GetDirectionsResponse>
}





