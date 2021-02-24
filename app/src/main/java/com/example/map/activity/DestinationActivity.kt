package com.example.map.activity

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.map.MainActivity
import com.example.map.R
import com.example.map.SetLocationUpdate
import com.example.map.base.BaseActivity
import com.example.map.fragment.EnterDestinationFragment
import com.example.map.fragment.HomeFragment
import com.example.map.fragment.Place
import com.google.android.libraries.places.api.Places
import org.koin.java.KoinJavaComponent

class DestinationActivity : BaseActivity() {
    private val setLocationUpdate: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)

    private val baseFragment = lazy {
        listOf(EnterDestinationFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination)
        Places.initialize(this, "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        supportActionBar?.hide()
        initFragNavController(this, baseFragment.value, DestinationActivity.TAG, supportFragmentManager, R.id.content2)

        //This code is to pass the value to Fragment
//        val currentAddress = intent.getStringExtra("current address")
//        println("Test CLLicked: $currentAddress")
//        setLocationUpdate.currentAddress.value = currentAddress

        //This code is to pass the value to Fragment
        val currentPlace = intent.getSerializableExtra("current place")
       setLocationUpdate.currentPlace.value = currentPlace as Place?



    }

    companion object{
        val TAG = this.javaClass.canonicalName ?: "Destination"
    }
}