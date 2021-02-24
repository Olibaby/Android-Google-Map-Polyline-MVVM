package com.example.map

import android.location.Location
import com.example.map.fragment.Place
import com.google.android.gms.maps.model.LatLng
import com.qucoon.rubiescircle.utils.SingleLiveEvent

data class SetLocationUpdate(
    val observer : SingleLiveEvent<String>,
    val SetLocationObserver:SingleLiveEvent<LatLng>,
    var pickup: String = "",
    var destination: String = "",
    var currentAddress: SingleLiveEvent<String>,
    var currentPlace: SingleLiveEvent<Place>
)