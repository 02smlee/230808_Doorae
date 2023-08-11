package com.example.busandorea

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.busandorea.adapter.MyAdapter
import com.example.busandorea.adapter.MyDecoration
import com.example.busandorea.databinding.FragmentTourListBinding
import com.example.busandorea.model.ItemTour

class TourListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding= FragmentTourListBinding.inflate(inflater, container, false)

        val datas = mutableListOf<ItemTour>()
//        for(i in 1..20){
//            datas.add("Item $i")
//        }

//        val layoutManager = LinearLayoutManager(activity)
//        binding.recyclerview.layoutManager=layoutManager
//        val adapter= MyAdapter(,datas)
//        binding.recyclerview.adapter=adapter
//        binding.recyclerview.addItemDecoration(MyDecoration(activity as Context))

        return binding.root
    }

}