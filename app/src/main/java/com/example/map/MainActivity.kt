package com.example.map

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.map.base.BaseActivity
import com.example.map.base.observeChange
import com.example.map.fragment.HomeFragment
import com.example.map.viewmodel.HomeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ncapdevi.fragnav.FragNavController
import kotlinx.android.synthetic.main.fragment_persistent_bottom_sheet.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import java.util.*


class MainActivity : BaseActivity() {

    private val setLocationUpdate: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    private val setDestinationObserver: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)
    private lateinit var sheetBehaviour: BottomSheetBehavior<ConstraintLayout>

    private val baseFragment = lazy {
        listOf(HomeFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Places.initialize(this, "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        initFragNavController(this, baseFragment.value, TAG, supportFragmentManager, R.id.content)
        initSheet()
        setUpLocationSearch()
    }


    private fun initSheet() {
        sheetBehaviour = BottomSheetBehavior.from(bottom_sheet)
        sheetBehaviour.isFitToContents = false
        sheetBehaviour.halfExpandedRatio = 0.6f
        setUpCallBacks()
    }

    private fun setUpCallBacks() {
        sheetBehaviour.setBottomSheetCallback(object :BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(view: View, p1: Float) {

            }

            override fun onStateChanged(view: View, newState: Int) {
                when(newState){
                    BottomSheetBehavior.STATE_HIDDEN -> println("Hidden")
                    BottomSheetBehavior.STATE_EXPANDED -> println("Expanded")
                    BottomSheetBehavior.STATE_COLLAPSED -> println("Collapsed")
                    BottomSheetBehavior.STATE_DRAGGING -> println("Started dragging")
                    BottomSheetBehavior.STATE_SETTLING -> println("Settled")
                }
            }

        })
    }


    private fun setUpLocationSearch() {
        editTextSearchLocation.setOnClickListener {
            println("you have clicked me o")
            setDestinationObserver.observer.value = "search"
            //launchAddersPicker()
        }
    }

    private fun launchAddersPicker() {
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, Arrays.asList(
                Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS,
                Place.Field.LAT_LNG)
        )
            .build(this)
        startActivityForResult(intent, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("activity called")
        if (requestCode == 1) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    Timber.i( "Place: " + place.name + ", " + place.id + place.address + place.addressComponents + ' ' + place.latLng?.longitude + ' ' + place.latLng?.latitude+ " " )

                    val addressComponent = place.addressComponents?.asList()
                    addressComponent?.let {
                        it.forEach { println(it) }
                    }

                    val completeAddress = addressComponent?.map { it.name }?.joinToString() ?: ""

                    // editTextTextPersonName.setText("${completeAddress}  ")
                    place.latLng?.let {
                        setLocationUpdate.SetLocationObserver.value = it
                        println(setLocationUpdate.SetLocationObserver.value)
                        //setNewLocation(it)
                    }

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Timber.i( status.statusMessage)
                    // mfragmentNavigation.openBottomDialogFragment(SingleChoiceQuestionBottomSheetFragment.newInstance("address","Could'nt find address","Yes, i want to type it myself","No Cancel"))

                }
                Activity.RESULT_CANCELED -> {
                    //  mfragmentNavigation.openBottomDialogFragment(SingleChoiceQuestionBottomSheetFragment.newInstance("address","Could'nt find address","Yes, i want to type it myself","No Cancel"))
                    // The user canceled the operation.
                }
                else -> println()  //mfragmentNavigation.openBottomDialogFragment(SingleChoiceQuestionBottomSheetFragment.newInstance("address","Could'nt find address","Yes, i want to type it myself","No Cancel"))
            }
        }
    }




    companion object{
        val TAG = this.javaClass.canonicalName ?: "Main"
    }

}