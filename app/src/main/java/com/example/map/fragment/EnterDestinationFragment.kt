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
    private lateinit var placesClient: PlacesClient
    private var predictionList = mutableListOf<AutocompletePrediction>()
    private var suggestionList = mutableListOf<String>()
    private val homeViewModel: HomeViewModel by sharedViewModel()
    private val setLocationUpdate: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    private var selectedPrediction: AutocompletePrediction? = null

//    val name = arguments?.getString(CURRENT_ADDR)


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
        activity!!.startActivity(intent)
    }


    private fun observerViewModel() {
//        homeViewModel.currentAddress.observeChange(viewLifecycleOwner){
//            editTextPickup.setText(it)
//        }
        setLocationUpdate.currentAddress.observeChange(viewLifecycleOwner){
            editTextPickup.setText(it)
        }

        homeViewModel.setPickup.observeChange(viewLifecycleOwner){
            editTextPickup.setText(it)
            setLocationUpdate.pickup = it
            //moveToMainActivity()
        }
        homeViewModel.setDestination.observeChange(viewLifecycleOwner){
            editTextDestination.setText(it)
            setLocationUpdate.destination = it
            selectedPrediction?.let {
                getLatLng(it)
            }
            //suggestionList.clear()
            //moveToMainActivity()
        }
    }

    private fun initializePlaces() {
        Places.initialize(requireContext(), "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        placesClient = Places.createClient(requireContext())
        println(placesClient)

    }

    private fun updateUI() {
//        if (!homeViewModel.currentAddress.isNullOrEmpty()){
//            editTextPickup.setText(homeViewModel.currentAddress)
//        } else{
//            println("No current address")
//        }

//        editTextPickup.setText(name)

        backBtn.setOnClickListener {
            moveToMainActivity()
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


          val requests = FindAutocompletePredictionsRequest.builder().setCountry("NG").setSessionToken(token).setQuery(query).build()

          placesClient.findAutocompletePredictions(requests).addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
              predictionList = response.autocompletePredictions
              suggestionList.clear()
              suggestionList.addAll(response.autocompletePredictions.map { it.getFullText(null).toString() })
              println("HERE ARE THE SUGGESTIONS $suggestionList")
              updateRecycler(selectType)

          }.addOnFailureListener { exception: Exception? ->
              println("calling place preciction fail")
                      if (exception is ApiException) {
                          Log.e(TAG, "Place not found: " + exception.statusCode)
                      }
                  }
      }

    private fun updateRecycler(selectType: SelectType){
        println("i have entered the recycler")
         DestinationRecycler.updateRecycler(requireContext(), suggestionList, R.layout.places_suggestion, listOf(R.id.suggestionsTextView),
                 {innerView, position ->

                     val name = innerView[R.id.suggestionsTextView] as TextView

                     name.text = suggestionList[position]

         }, {position ->
             val address = suggestionList[position]
             selectedPrediction = predictionList[position]

             if (selectType == SelectType.PICKUP){
                 homeViewModel.setPickup.value = address
             } else{
                 homeViewModel.setDestination.value = address
             }

         })
    }

    private fun getLatLng(selectedPrediction: AutocompletePrediction){
        println("i'm trying to get latlng")
        var placeId = selectedPrediction.placeId
        var placeFields = Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG)
        var fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build()
        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener {
            var place = it.place
            println("latlng is ${place.latLng}, address is ${place.address}")
              homeViewModel.getLatLng.value = place.latLng
//            setLocationUpdate.getLatLng.value = place.latLng
        }.addOnFailureListener {
            println(it)
        }
    }



    companion object {
//        const val CURRENT_ADDR = "current address"
//
//
//        fun newInstance(currentAddress: String): EnterDestinationFragment {
//            val fragment = EnterDestinationFragment()
//
//            val bundle = Bundle().apply {
//                putString(CURRENT_ADDR, currentAddress)
//            }
//
//            fragment.arguments = bundle
//
//            return fragment
//        }
    }
}