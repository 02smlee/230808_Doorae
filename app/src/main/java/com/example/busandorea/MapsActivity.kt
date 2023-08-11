package com.example.busandorea

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.busandorea.databinding.ActivityMapsBinding
import com.example.busandorea.fragment.CurrentMap
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // SupportMapFragment를 가져오고 지도를 사용할 준비가 되면 알림 받기/ Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 클릭 후 프레그먼트 전환 이벤트 처리
        binding.btnOnMap.setOnClickListener {
            val inputText = binding.messageEdit.text.toString()
            Log.d("smlee","버튼 클릭")

            // CurrentMap 프래그먼트로 전달할 데이터를 번들에 담기.
            val bundle = Bundle()
            bundle.putString("inputText", inputText)

            // CurrentMap 프래그먼트를 생성하고 번들을 전달.
            val currentMapFragment = CurrentMap()
            currentMapFragment.arguments = bundle

            // CurrentMap 프래그먼트를 화면에 추가.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, currentMapFragment)
                .commit()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}