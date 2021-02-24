package com.example.map.repository

import com.example.map.base.BaseRepository
import com.example.map.model.request.GetDirectionsRequest
import com.example.map.model.response.GetDirectionsResponse
import com.example.map.network.DirectionsAPI
import com.example.map.utils.UseCaseResult

interface HomeRepository{
    suspend fun getDirectionsAPI(request: GetDirectionsRequest): UseCaseResult<GetDirectionsResponse>
}

class HomeRepositoryImpl(private val directionsAPI: DirectionsAPI): BaseRepository(), HomeRepository{

    override suspend fun getDirectionsAPI(request: GetDirectionsRequest): UseCaseResult<GetDirectionsResponse> {
         val mResponse = directionsAPI.getDirectionsAPI(request.origin, request.destination, request.key)
        return safeGetApiCall(mResponse){it.status == "OK"}
    }
}