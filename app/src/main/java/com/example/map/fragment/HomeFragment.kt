package com.example.map.fragment

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.map.R
import com.example.map.SetLocationUpdate
import com.example.map.activity.DestinationActivity
import com.example.map.base.BaseFragment
import com.example.map.base.observeChange
import com.example.map.utils.CheckPermissionUtil
import com.example.map.viewmodel.HomeViewModel
import com.github.euzee.permission.PermissionCallback
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.nubis.watchguard.utils.locationutils.LocationHelper
import com.qucoon.watchguard.utils.locationutils.LocationManager
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.java.KoinJavaComponent
import java.util.*

class HomeFragment : BaseFragment(), OnMapReadyCallback , LocationManager{
    lateinit var locationHelper: LocationHelper
    private val homeViewModel:HomeViewModel by sharedViewModel()
    private val setLocationUpdate: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    private val setDestinationObserver: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    var mGoogleMap: GoogleMap? = null
    var mapView: MapView? = null


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
//            intent.putExtra("current address", homeViewModel.currentAddress.value)
            activity!!.startActivity(intent)
        }

        homeViewModel.getLatLng.observeChange(viewLifecycleOwner){
            setNewLocation(it)
        }
//        setLocationUpdate.getLatLng.observeChange(viewLifecycleOwner){
//            setNewLocation(it)
//        }
    }

    private fun initViews() {
        mapView = view?.findViewById<View>(R.id.mapView2) as MapView
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


        addMarker(6.436160, 3.523290)
        addMarker(6.436160, 3.503290)
        addMarker(6.436160, 3.522290)
        addMarker(6.436160, 3.533290)



    }

    fun addMarker(latitude:Double,longitude:Double){
        val latLng = LatLng(latitude, longitude)
        mGoogleMap?.addMarker(MarkerOptions().position(latLng).title("Marker Title"))
    }



    private fun setNewLocation(latLng: LatLng) {
        homeViewModel.showMyCurrentLocation = false
        zoomInMap(latLng.latitude,latLng.longitude)
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
                getAddressFromLatLng(location.latitude, location.longitude)
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
        val country: String = addresses[0].getCountryName()
        val postalCode: String = addresses[0].getPostalCode()
        val knownName: String = addresses[0].getFeatureName() // Only if available else return NULL

        homeViewModel.currentAddress.value = address + city + state
        println("current address is ${homeViewModel.currentAddress.value}")
    }


}

