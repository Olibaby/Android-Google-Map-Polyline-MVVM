package com.example.map.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.map.MainActivity
import com.example.map.R
import com.example.map.base.BaseActivity
import com.example.map.fragment.EnterDestinationFragment
import com.example.map.fragment.HomeFragment
import com.google.android.libraries.places.api.Places

class DestinationActivity : BaseActivity() {

    private val baseFragment = lazy {
        listOf(EnterDestinationFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_destination)
        Places.initialize(this, "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg")
        supportActionBar?.hide()
        initFragNavController(this, baseFragment.value, DestinationActivity.TAG, supportFragmentManager, R.id.content2)

//        val currentAddress = intent.getStringExtra("current address")
//        println("Test CLLicked: $currentAddress")

        //This code is to pass the value to Fragment
//        currentAddress?.let { EnterDestinationFragment.newInstance(it) }
    }

    companion object{
        val TAG = this.javaClass.canonicalName ?: "Destination"
    }
}