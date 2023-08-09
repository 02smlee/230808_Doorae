package com.example.busandorea.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import com.example.busandorea.MapsActivityCurrentPlace
import com.example.busandorea.R
import com.example.busandorea.databinding.CurrentMapBinding
import org.checkerframework.checker.units.qual.C
import kotlin.math.log

//findViewById로 맵 엑티비티 뷰를 호출한 코드
/*class CurrentMap : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.current_map, container, false)
        val btnCurrentMap = rootView.findViewById<Button>(R.id.btn_currentmap)
        btnCurrentMap.setOnClickListener{
            // 버튼 클릭 시 MapsActivityCurrentPlace 엑티비티를 실행하는 코드
            Log.d("smlee","test : item click : ${it}")
            startActivity(Intent(context, MapsActivityCurrentPlace::class.java))
        }
        return rootView
    }
}*/

// binding 활용 코드의 경우
class CurrentMap : Fragment() {

    private var root_binding: CurrentMapBinding? = null
    private val binding get() = root_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root_binding = CurrentMapBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.btnCurrentmap.setOnClickListener {
            Log.d("smlee","test : 내 현재 위치 지도 click : ${it}")
            startActivity(Intent(requireContext(), MapsActivityCurrentPlace::class.java))
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        root_binding = null
    }
}

