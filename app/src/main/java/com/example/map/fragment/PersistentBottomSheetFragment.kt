package com.example.map.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.map.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PersistentBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        println("CREATED 2")
        return inflater.inflate(R.layout.fragment_persistent_bottom_sheet, container, false)
    }

    companion object {

    }
}