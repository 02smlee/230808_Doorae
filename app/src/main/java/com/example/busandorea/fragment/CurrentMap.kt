package com.example.busandorea.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import com.example.busandorea.MapsActivityCurrentPlace
import com.example.busandorea.R
import com.example.busandorea.databinding.CurrentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var map: GoogleMap // map 객체 생성하는 코드가 생략됨

    private var root_binding: CurrentMapBinding? = null
    private val binding get() = root_binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root_binding = CurrentMapBinding.inflate(inflater, container, false)
        val rootView = binding.root
        val inputText = arguments?.getString("inputText")
        binding.outputText.text = inputText // 전달받은 데이터를 TextView에 설정

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

