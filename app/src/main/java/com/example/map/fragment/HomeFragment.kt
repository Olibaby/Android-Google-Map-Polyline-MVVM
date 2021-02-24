package com.example.map.fragment

import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.map.R
import com.example.map.SetLocationUpdate
import com.example.map.activity.DestinationActivity
import com.example.map.base.BaseFragment
import com.example.map.base.observeChange
import com.example.map.utils.CheckPermissionUtil
import com.example.map.viewmodel.HomeViewModel
import com.github.euzee.permission.PermissionCallback
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.nubis.watchguard.utils.locationutils.LocationHelper
import com.qucoon.watchguard.utils.locationutils.LocationManager
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.java.KoinJavaComponent
import java.io.Serializable
import java.util.*


class HomeFragment : BaseFragment(), OnMapReadyCallback , LocationManager{
    lateinit var locationHelper: LocationHelper
    private val homeViewModel:HomeViewModel by sharedViewModel()
    private val setLocationUpdate: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    private val setDestinationObserver: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    lateinit var currentAddress: String
    lateinit var currentPlace: Place
    var mGoogleMap: GoogleMap? = null
    var mapView: MapView? = null
    private var latLngs = mutableListOf<LatLng>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
      ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Places.initialize(requireContext(), "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        initViews()
        initLocationServices()
        observeViewModel()
    }

    private fun observeViewModel() {
                  //Google autocomplete observer
//        setLocationUpdate.SetLocationObserver.observeChange(viewLifecycleOwner){
//            println("i am observing")
//            setNewLocation(it)
//        }

        setDestinationObserver.observer.observeChange(viewLifecycleOwner){
            val intent = Intent(activity, DestinationActivity::class.java)
            intent.putExtra("current place", currentPlace)
            startActivityForResult(intent,1)
        }

        homeViewModel.getLocation.observeChange(viewLifecycleOwner){
             getAddressFromLatLng(it.latitude, it.longitude)
        }

        homeViewModel.getDirectionsResponse.observeChange(viewLifecycleOwner){
            println("directions response oberved")
            var parsedString = homeViewModel.parseResponseToString(it)
            var hashMapList = homeViewModel.convertStringToJson(parsedString)
            drawLine(hashMapList)
        }
    }

    private fun initViews() {
        mapView = view?.findViewById<View>(R.id.mapView2) as MapView
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val pickuplat = data?.getDoubleExtra("pickupLat",0.0) ?: return
        val pickuplon = data?.getDoubleExtra("pickupLon",0.0) ?: return

        val lat = data?.getDoubleExtra("destinationLat",0.0) ?: return
        val lon = data?.getDoubleExtra("destinationLon",0.0) ?: return

        println("pickup lat is $pickuplat and lng is $pickuplon")
        println("destination lat is $lat and lng is $lon")

        //setNewLocation(LatLng(lat,lon))
        setMarkerOnLocations(LatLng(pickuplat,pickuplon), "pickup")
        setMarkerOnLocations(LatLng(lat,lon), "destination")

        val origin = "$pickuplat,$pickuplon"
        val destination = "$lat,$lon"
        homeViewModel.getDirections(origin, destination)
    }

    private fun initLocationServices() {

        CheckPermissionUtil.checkLocation(context!!,object:PermissionCallback(){
            override fun onPermissionGranted() {
                locationHelper = LocationHelper(activity!!, this@HomeFragment)
                locationHelper.startLocationUpdates()
                if (mapView != null) {
                    // Initialise the MapView
                    mapView!!.onCreate(null)
                    mapView!!.onResume()
                    // Set the map ready callback to receive the GoogleMap object
                    mapView!!.getMapAsync(this@HomeFragment)

                }
            }

            override fun onPermissionDenied() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

    }



    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context!!)
        mGoogleMap = p0
        mGoogleMap?.isMyLocationEnabled = true
        mGoogleMap?.uiSettings?.isZoomControlsEnabled = false
        mGoogleMap?.uiSettings?.isZoomGesturesEnabled = true
        mGoogleMap?.uiSettings?.isMyLocationButtonEnabled = true


 //       addMarker(6.436160, 3.523290)
//        addMarker(6.436160, 3.503290)
//        addMarker(6.436160, 3.522290)
//        addMarker(6.436160, 3.533290)
    }

    fun addMarker(latitude:Double,longitude:Double){
        val latLng = LatLng(latitude, longitude)
        mGoogleMap?.addMarker(MarkerOptions().position(latLng).title("Marker Title"))
    }



    private fun setNewLocation(latLng: LatLng) {
        homeViewModel.showMyCurrentLocation = false
        zoomInMap(latLng.latitude,latLng.longitude)
        mGoogleMap?.addMarker(MarkerOptions().position(latLng).title("destination"))
    }

    private fun setMarkerOnLocations(latLng: LatLng, type: String) {
        if (type == "pickup"){
            latLngs.add(latLng)
        } else if(type == "destination"){
            latLngs.add(latLng)
        }

        when (latLngs.count()){
            2 -> {
                println("they are up to two")
                //mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0],8f))
                //mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 8f))

                var latLngBounds = LatLngBounds.Builder().include(latLngs[0]).include(latLngs[1]).build()
                println("camera about to move")
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10))

                val position = CameraPosition.Builder()
                        .target(midPoint(lat1 = latLngs[0].latitude, long1 = latLngs[0].longitude, lat2 = latLngs[1].latitude, long2 = latLngs[1].longitude))
                        .zoom(15.5f)
                        .bearing(0f)
                        .tilt(25f)
                        .build()
                println("camera about to move")
                mGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position))
                println("camera moved")

                mGoogleMap?.addMarker(MarkerOptions().position(latLngs[0]).title("pickup"))
                mGoogleMap?.addMarker(MarkerOptions().position(latLngs[1]).title("destination"))


            }
            else -> println("they are not up to two yet o")
        }

    }

    private fun midPoint(lat1: Double, long1: Double, lat2: Double, long2: Double): LatLng? {
        return LatLng((lat1 + lat2) / 2, (long1 + long2) / 2)
    }

    fun zoomInMap(latitude:Double,longitude:Double){
        val latLng = LatLng(latitude, longitude)
        val cameraPosition = CameraPosition.Builder().target(latLng).zoom(15f).build()
        mGoogleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }

    override fun onLocationChanged(location: Location?) {
        setCameraOnLocation(location)

    }

    private fun setCameraOnLocation(location: Location?) {
        location?.let {
            val currentLatLng = LatLng(location.latitude, location.longitude)
            if (homeViewModel.showMyCurrentLocation){
                mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                 homeViewModel.getLocation.value = location
            }

        }
    }

    override fun getLastKnownLocation(location: Location?) {
        setCameraOnLocation(location)
    }


    private fun getAddressFromLatLng(lat: Double, lng: Double){
        var geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addresses = mutableListOf<Address>()

        addresses = geocoder.getFromLocation(lat, lng, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        val address: String = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        val city: String = addresses[0].getLocality()
        val state: String = addresses[0].getAdminArea()

       // Place(lat,lng,currentAddress)
         currentAddress = address + city + state
         currentPlace = Place(lat, lng, currentAddress)
    }

    fun drawLine(lists: List<List<HashMap<String, String>>> ){
        var points: List<LatLng>? = null
        var polylineOptions: PolylineOptions? = null

        for (path in lists) {
            points = ArrayList()
            polylineOptions = PolylineOptions()
            for (point in path) {
                val lat = point["lat"]!!.toDouble()
                val lon = point["lng"]!!.toDouble()
                points.add(LatLng(lat, lon))
            }
            polylineOptions.addAll(points)
            polylineOptions.width(15f)
            polylineOptions.color(Color.BLUE)
            polylineOptions.geodesic(true)
        }
        if (polylineOptions != null) {
            mGoogleMap?.addPolyline(polylineOptions)
        } else {
            Toast.makeText(
                requireContext(),
                "Direction not found",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}

  data class Place(val lat: Double, val lng: Double, val address: String) : Serializable


