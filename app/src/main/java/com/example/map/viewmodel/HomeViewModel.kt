package com.example.map.viewmodel

import com.example.map.base.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.qucoon.rubiescircle.utils.SingleLiveEvent

class HomeViewModel: BaseViewModel() {
    var setPickup = SingleLiveEvent<String>()
    var setDestination = SingleLiveEvent<String>()
}