package com.example.map.model.request

data class GetDirectionsRequest(
    val origin: String,  val destination: String, val key: String
)
