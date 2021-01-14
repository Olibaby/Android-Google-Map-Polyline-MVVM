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
import com.example.map.activity.DestinationActivity.Companion.TAG
import com.example.map.base.BaseFragment
import com.example.map.base.observeChange
import com.example.map.utils.updateRecycler
import com.example.map.viewmodel.HomeViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.fragment_enter_destination.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class EnterDestinationFragment : BaseFragment() {
    //private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    //private var predictionList = mutableListOf<AutocompletePrediction>()
    private var suggestionList = mutableListOf<String>()
    private val homeViewModel: HomeViewModel by viewModel()


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

    private fun observerViewModel() {
        homeViewModel.setPickup.observeChange(viewLifecycleOwner){
            editTextPickup.setText(it)
        }
        homeViewModel.setDestination.observeChange(viewLifecycleOwner){
            editTextDestination.setText(it)
        }
    }

    private fun initializePlaces() {
        //mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        Places.initialize(requireContext(), "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        placesClient = Places.createClient(requireContext())
        println(placesClient)

    }

    private fun updateUI() {
        backBtn.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            activity!!.startActivity(intent)
        }

        val textWatcher1 = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getPlacePrediction(s.toString(), SelectType.PICKUP)
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

          // Create a RectangularBounds object.
          val bounds = RectangularBounds.newInstance(
                  LatLng(-33.880490, 151.184363),
                  LatLng(-33.858754, 151.229596)
          )
          // Use the builder to create a FindAutocompletePredictionsRequest.
          // Call either setLocationBias() OR setLocationRestriction().
          //.setLocationBias(bounds)
          val request =  FindAutocompletePredictionsRequest.builder()
                          .setLocationRestriction(bounds)
                          //.setOrigin(LatLng(-33.8749937, 151.2041382))
                        //  .setCountry("NG")
                         // .setTypeFilter(TypeFilter.ADDRESS)
                          .setSessionToken(token)
                          .setQuery(query)
                          .build()
          val requests = FindAutocompletePredictionsRequest.builder().setCountry("NG").setSessionToken(token).setQuery(query).build()

          placesClient.findAutocompletePredictions(requests).addOnSuccessListener { response: FindAutocompletePredictionsResponse ->

              suggestionList.clear()
              suggestionList.addAll(response.autocompletePredictions.map { it.getFullText(null).toString() })
              println("HERE ARE THE SUGGESTIONS ${suggestionList}")
              updateRecycler(selectType)
                      //predictionList = response.autocompletePredictions
//                      for (prediction in response.autocompletePredictions) {
//                          println("calling place preciction success inside")
//
//
////                          Log.i(TAG, prediction.placeId)
////                          Log.i(TAG, prediction.getPrimaryText(null).toString())
////                          println("calling place preciction success log")
////
//                          suggestionList.add(prediction.getFullText(null).toString())
//                          println(suggestionList)
//                          updateRecycler()
//                      }
                  }.addOnFailureListener { exception: Exception? ->
              println("calling place preciction fail")
                      if (exception is ApiException) {
                          Log.e(TAG, "Place not found: " + exception.statusCode)
                      }
                  }
      }

    private fun updateRecycler(selectType: SelectType){
         DestinationRecycler.updateRecycler(requireContext(), suggestionList, R.layout.places_suggestion, listOf(R.id.suggestionsTextView),
                 {innerView, position ->

                     val name = innerView[R.id.suggestionsTextView] as TextView

                     name.text = suggestionList[position]

         }, {position ->
             val address = suggestionList[position]
             if (selectType == SelectType.PICKUP){
                 homeViewModel.setPickup.value = address
             } else{
                 homeViewModel.setDestination.value = address
             }
         })
    }

    companion object {

    }
}