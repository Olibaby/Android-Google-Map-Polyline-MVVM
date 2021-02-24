package com.example.map.viewmodel

import android.location.Location
import com.example.map.DirectionParser
import com.example.map.base.BaseViewModel
import com.example.map.model.request.GetDirectionsRequest
import com.example.map.model.response.GetDirectionsResponse
import com.example.map.repository.HomeRepository
import com.google.gson.Gson
import com.qucoon.rubiescircle.utils.SingleLiveEvent
import org.json.JSONObject


class HomeViewModel(private val homeRepository: HomeRepository): BaseViewModel() {
    var showMyCurrentLocation = true
    var getLocation = SingleLiveEvent<Location>()

    val getDirectionsResponse = SingleLiveEvent<GetDirectionsResponse>()

    fun getDirections(origin: String, destination: String){
        val request = GetDirectionsRequest(origin, destination, "AIzaSyDQeKBxAcffS4RAdmSSoq7ZjdIWZ17L_Js")
        apiRequest(request, homeRepository::getDirectionsAPI, getDirectionsResponse, {it.status})
    }

    fun parseResponseToString(response: GetDirectionsResponse): String{

        val gson = Gson() // Or use new GsonBuilder().create();
        return gson.toJson(response)

        //val target2: GetDirectionsResponse = gson.fromJson(gson.toJson(response), GetDirectionsResponse::class.java) // deserializes json into target2
        //return response.toString()
    }

    fun convertStringToJson(direction: String) : List<List<HashMap<String, String>>> {
//        val convert = direction.replace("\\\"", "'")
//        val jo = JSONObject(convert.substring(1, convert.length - 1))

        val jsonObject = JSONObject(direction)
        val parser = DirectionParser()
//        return parser.parse(jo)
        return parser.parse(jsonObject)
    }
}