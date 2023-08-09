package com.example.busandorea.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment

class PlaceDetailsFragment : Fragment() {
    // ...

    companion object {
        fun newInstance(placeName: String?, placeAddress: String?): PlaceDetailsFragment {
            val fragment = PlaceDetailsFragment()
            val args = Bundle()
            args.putString("placeName", placeName)
            args.putString("placeAddress", placeAddress)
            fragment.arguments = args
            return fragment
        }
    }
}