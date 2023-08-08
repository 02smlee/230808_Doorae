package com.example.busandorea.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.busandorea.R
import com.example.busandorea.databinding.CurrentMapBinding
import org.checkerframework.checker.units.qual.C


class CurrentMap : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.current_map, container, false)
    }
}