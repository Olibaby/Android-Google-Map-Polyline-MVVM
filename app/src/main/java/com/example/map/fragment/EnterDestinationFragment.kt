package com.example.map.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.map.MainActivity
import com.example.map.R
import com.example.map.SetLocationUpdate
import com.example.map.activity.DestinationActivity.Companion.TAG
import com.example.map.base.BaseFragment
import com.example.map.base.observeChange
import com.example.map.utils.updateRecycler
import com.example.map.viewmodel.HomeViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.*
import kotlinx.android.synthetic.main.fragment_enter_destination.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.util.*


class EnterDestinationFragment : BaseFragment() {
    private var placesClient: PlacesClient? = null
    private var predictionList = mutableListOf<AutocompletePrediction>()
    private var suggestionList = mutableListOf<String>()
    private val homeViewModel: HomeViewModel by sharedViewModel()
    private val setLocationUpdate: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    private var selectedPrediction: AutocompletePrediction? = null


    private var latlngList = mutableListOf<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        initializePlaces()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_enter_destination, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializePlaces()
        updateUI()
        observerViewModel()
        setUpObservers(homeViewModel)
    }

    private fun moveToMainActivity(){
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("current destination", setLocationUpdate.destination)
        println("i'm passing $setLocationUpdate.destination")
        activity!!.startActivity(intent)
    }


    private fun observerViewModel() {
//        setLocationUpdate.currentAddress.observeChange(viewLifecycleOwner){
//            editTextPickup.setText(it)
//        }

        setLocationUpdate.currentPlace.observeChange(viewLifecycleOwner){
            println("CURRENT LOCATION - ${latlngList}")
            editTextDestination.requestFocus()
            editTextPickup.setText(it.address)
            val latlng = LatLng(it.lat, it.lng)
            latlngList.add(0, latlng)
        }
    }


    private fun initializePlaces() {
        Places.initialize(requireContext(), "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        placesClient = Places.createClient(requireContext())
        println(placesClient)

    }

    private fun updateUI() {
        backBtn.setOnClickListener {
            moveToMainActivity()
        }

        //suggestionList.clear()

        val textWatcher1 = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(editTextPickup.hasFocus()){
                    for (lats in latlngList){
                        println("this are the lats $lats")
                        println("there are ${latlngList.count()} lats at the moment")
                    }
                    getPlacePrediction(s.toString(), SelectType.PICKUP)
                }

            }
        }

        editTextPickup.addTextChangedListener(textWatcher1)

        val textWatcher2 = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getPlacePrediction(s.toString(), SelectType.DESTINATION)
            }
        }

        editTextDestination.addTextChangedListener(textWatcher2)

    }

    enum class SelectType{
        PICKUP,
        DESTINATION
    }



      fun getPlacePrediction(query: String, selectType: SelectType){
          println("get place")
          // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
          // and once again when the user makes a selection (for example when calling fetchPlace()).
          val token = AutocompleteSessionToken.newInstance()


          val requests = FindAutocompletePredictionsRequest.builder().setCountry("NG").setSessionToken(token).setQuery(query).build()

          placesClient?.findAutocompletePredictions(requests)?.addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
              predictionList = response.autocompletePredictions
              suggestionList.clear()
              suggestionList.addAll(response.autocompletePredictions.map { it.getFullText(null).toString() })
              println("HERE ARE THE SUGGESTIONS $suggestionList")
              updateRecycler(selectType)

          }?.addOnFailureListener { exception: Exception? ->
              println("calling place preciction fail")
                      if (exception is ApiException) {
                          Log.e(TAG, "Place not found: " + exception.statusCode)
                      }
                  }
      }

    private fun updateRecycler(selectType: SelectType){
        println("i have entered the recycler")
        println(suggestionList)
         DestinationRecycler.updateRecycler(requireContext(), suggestionList, R.layout.places_suggestion, listOf(R.id.suggestionsTextView),
                 {innerView, position ->

                     val name = innerView[R.id.suggestionsTextView] as TextView

                     name.text = suggestionList[position]

         }, {position ->
             val address = suggestionList[position]
             selectedPrediction = predictionList[position]
                 suggestionList.clear()

                 if (selectType == SelectType.PICKUP){
                 editTextPickup.setText(address)
                 setLocationUpdate.pickup = address
                 selectedPrediction?.let {
//                     suggestionList.clear()
                     getLatLng(it, "pickup")
                 }
             } else{
                 editTextDestination.setText(address)
                 setLocationUpdate.destination = address
                 selectedPrediction?.let {
//                     suggestionList.clear()
                     getLatLng(it, "destination")
                 }
             }

         })
    }


    override fun onDestroy() {
        super.onDestroy()
        placesClient = null

        println("THIS FRAGMENT WAS DESTROYED ")
    }

    private fun getLatLng(selectedPrediction: AutocompletePrediction, type: String){
        println("i'm trying to get latlng")
        var placeId = selectedPrediction.placeId
        var placeFields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
        var fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient?.fetchPlace(fetchPlaceRequest)?.addOnSuccessListener {
            var place = it.place
            println("latlng is ${place.latLng}, address is ${place.address}")

//            if (type == "pickup"){
//                //Place(place.latLng.latitude,place.latLng.longitude,place.address)
//                latlngList.add(0, place)
//            } else if (type == "destination"){
//                latlngList.add(1, place)
//            }
//
//            when(latlngList.count()){
//                2 -> {
//                    suggestionList.clear()
//                    val intent = Intent()
//                    intent.putExtra("pickupLat",latlngList[0].latLng?.latitude)
//                    intent.putExtra("pickupLon",latlngList[0].latLng?.longitude)
//
//                    intent.putExtra("destinationLat",latlngList[1].latLng?.latitude)
//                    intent.putExtra("destinationLon",latlngList[1].latLng?.longitude)
//                    requireActivity().setResult(1,intent)
//                    requireActivity().finish()
//                }
//                else -> {
//                    println("count less than 2")
//                }
//            }


            if (type == "pickup"){
                //Place(place.latLng.latitude,place.latLng.longitude,place.address)
                place.latLng?.let { it1 -> latlngList.set(0, it1) }
            } else if (type == "destination"){
                place.latLng?.let { it1 -> latlngList.add(1, it1) }
            }

            when(latlngList.count()){
                2 -> {
                    suggestionList.clear()
                    val intent = Intent()
                    intent.putExtra("pickupLat",latlngList[0].latitude)
                    intent.putExtra("pickupLon",latlngList[0].longitude)

                    intent.putExtra("destinationLat",latlngList[1].latitude)
                    intent.putExtra("destinationLon",latlngList[1].longitude)
                    requireActivity().setResult(1,intent)
                    requireActivity().finish()
                }
                else -> {
                    println("count less than 2")
                }
            }
        }?.addOnFailureListener {
            println(it)
        }

    }




    companion object {
    }
}