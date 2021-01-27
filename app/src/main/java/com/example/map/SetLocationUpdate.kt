package com.example.map

import com.google.android.gms.maps.model.LatLng
import com.qucoon.rubiescircle.utils.SingleLiveEvent

data class SetLocationUpdate(
    val observer : SingleLiveEvent<String>,
    val SetLocationObserver:SingleLiveEvent<LatLng>,
    var pickup: String = "",
    var destination: String = "",
    var getLatLng: SingleLiveEvent<LatLng>
)