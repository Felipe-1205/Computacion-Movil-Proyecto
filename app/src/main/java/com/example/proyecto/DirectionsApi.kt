package com.example.proyecto

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {

    @GET("/v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") key: String,
        @Query("start",encoded = true) start: String,
        @Query("end",encoded = true) end: String

    ): Response<RouteResponse>
}