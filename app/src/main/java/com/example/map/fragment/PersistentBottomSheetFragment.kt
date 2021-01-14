package com.example.map.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.map.R
import com.example.map.SetLocationUpdate
import com.example.map.base.BaseBottomSheetFragment
import com.example.map.base.observeChange
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_persistent_bottom_sheet.*
import org.koin.java.KoinJavaComponent

class PersistentBottomSheetFragment : BaseBottomSheetFragment() {
    private val setDestinationObserver: SetLocationUpdate by KoinJavaComponent.inject(SetLocationUpdate::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        println("CREATED 2")
        return inflater.inflate(R.layout.fragment_persistent_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("i am on view")
    }


    private fun updateUI() {
        editTextSearchLocation.setOnClickListener {
            println("you have clicked me o")
           mFragmentNavigation.pushFragment(EnterDestinationFragment())
        }
    }

    companion object {

    }
}