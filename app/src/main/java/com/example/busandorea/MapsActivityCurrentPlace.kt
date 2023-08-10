package com.example.busandorea

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

import com.example.busandorea.fragment.PlaceDetailsFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

/** 사용자의 현재 위치를 구글맵에서 보여주기 */
class MapsActivityCurrentPlace : AppCompatActivity(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    private lateinit var mapController: NavController

    // Places API에 대한 진입점입니다.
    private lateinit var placesClient: PlacesClient

    // Fused Location Provider에 대한 진입점입니다.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // 위치 권한이 있을 때 사용할 기본 위치(호주 시드니) 및 기본 확대/축소
    // 허용되지 않았습니다.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    // 장치가 현재 위치한 지리적 위치. 즉, 마지막으로 알려진
    // Fused Location Provider에서 검색한 위치입니다.
    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)

    // [START maps_current_place_on_create]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [START_EXCLUDE silent]
        //  저장된 인스턴스 상태에서 위치 및 카메라 위치를 검색합니다.
        // [START maps_current_place_on_create_save_instance_state]
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        // [END maps_current_place_on_create_save_instance_state]
        // [END_EXCLUDE]

        // 지도를 렌더링하는 콘텐츠 보기를 검색합니다.
        setContentView(R.layout.activity_maps)

        // [START_EXCLUDE silent]
        //PlacesClient 구성
        Places.initialize(applicationContext, "BuildConfig.MAPS_API_KEY")
        placesClient = Places.createClient(this)

        //FusedLocationProviderClient를 구성합니다.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Build the map.
        // [START maps_current_place_map_fragment]
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        // [END maps_current_place_map_fragment]
        // [END_EXCLUDE]
    }
    // [END maps_current_place_on_create]

    /**
     * 활동이 일시 중지되었을 때 지도의 상태를 저장합니다.
     */
    // [START maps_current_place_on_save_instance_state]
    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }
    // [END maps_current_place_on_save_instance_state]

    /**
     * 옵션 메뉴 설정 Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.current_place_menu, menu)
        return true
    }

    /**
     * 장소를 얻기 위해 메뉴 옵션의 클릭을 처리합니다.
     * @param item 처리할 메뉴 항목.
     * @return Boolean
     */
    // [START maps_current_place_on_options_item_selected]
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_place) {
            showCurrentPlace()
        }
        return true
    }
    // [END maps_current_place_on_options_item_selected]

    /**
     사용 가능한 경우 지도를 조작합니다.
     * 이 콜백은 맵을 사용할 준비가 되면 트리거됩니다.
     */
    // [START maps_current_place_on_map_ready]
    override fun onMapReady(map: GoogleMap) {
        this.map = map
//        map.setOnMarkerClickListener(this)


        // 위치 권한을 확인하고 권한이 있을 경우에만 현재 위치 표시
        getLocationPermission()

        // 위치 권한이 있으면 현재 위치 정보 가져오기
        if (locationPermissionGranted) {
            getDeviceLocation()
        }



        // [START_EXCLUDE]
        // [START map_current_place_set_info_window_adapter]
        // 맞춤 정보 창 어댑터를 사용하여 정보 창 내용 핸들링

        this.map?.setInfoWindowAdapter(object : InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
                    findViewById<FrameLayout>(R.id.map), false)
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })


        // [END map_current_place_set_info_window_adapter]

        // Prompt the user for permission.
        getLocationPermission()
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }
    // [END maps_current_place_on_map_ready]

// 마커 클릭시 프래그먼트로 이동
//    override fun onMarkerClick(marker: Marker): Boolean {
//
//        // 마커가 클릭되었을 때 동작을 구현합니다.
//        if (marker?.title == "나의 현재 위치") {
//
//            // 여기에 마커 클릭 시 동작을 작성하세요.
//            // 예: 프래그먼트 전환 등
//            val mapController = findNavController(R.id.map)
//            mapController.navigate(R.id.currentmap_fragment)
//        }
//        return true
//    }

    /**
     * 기기의 현재 위치를 가져오고 지도의 카메라 위치를 지정합니다.
     */
    // [START maps_current_place_get_device_location]
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            // 여기서 LatLng 객체 생성
                            val currentLocation = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)

                            //지도에 마커 추가
                            val marker = map?.addMarker(
                                MarkerOptions()
                                    .position(currentLocation)
                                    .title("나의 현재 위치")
                                    .snippet("현재 위치입니다.")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                            )

                            this.map?.setInfoWindowAdapter(object : InfoWindowAdapter {
                                // Return null here, so that getInfoContents() is called next.
                                override fun getInfoWindow(arg0: Marker): View? {
                                    return null
                                }
                                override fun getInfoContents(marker: Marker): View {
                                    // Inflate the layouts for the info window, title and snippet.
                                    val infoWindow = layoutInflater.inflate(R.layout.custom_info_contents,
                                        findViewById<FrameLayout>(R.id.map), false)
                                    val title = infoWindow.findViewById<TextView>(R.id.title)
                                    title.text = marker.title
                                    val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                                    snippet.text = marker.snippet
                                    return infoWindow
                                }
                            })



                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))

                            false
                            }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }

            }
        } catch (e: SecurityException) {
            Log.e("Exception: 위치정보 권한 획득 실패", e.message, e)
        }

        /*Android Google Places 자동 완성 기능
           * 구글맵 위에 검색시 자동완성
           * AutocompleteSupportFragment 처리를 위한 fragment를 MapsActivityCurrentPlace.xml에 추가함.
                <fragment android:id="@+id/autocomplete_fragment"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:name="com.google.android.libraries.places.widget.AutocompleteSupportFragment" />
           * */

        //AutocompleteSupportFragment 초기화
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // 반환할 장소 데이터 타입 명시 Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

        // 응답을 처리할 PlaceSelectionListener 셋업 Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }
            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
        //자동 완성 기능 여기까지

        /*자동완성으로 키워드 검색식 프로그래매틱 방식으로 장소 예상 검색어 가져오기 (공식문서 아래)
            https://developers.google.com/maps/documentation/places/android-sdk/autocomplete?hl=ko#option_1_embed_an_autocompletesupportfragment
        * */


        // 자동 완성 세션에 대한 새 토큰을 만든 후 FindAutocompletePredictionsRequest에 전달 /Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // 그리고 예를들어 fetchPlace()를 호출할 때처럼 사용자가 선택할 때 다시 한 번 전달 /and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // 직사각형의 테두리를 지닌 객체 생성 Create a RectangularBounds object.
        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),
            LatLng(-33.858754, 151.229596)
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val query = null
        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(LatLng(35.156016, 129.059408))
                .setCountries("KR", "JP")
                .setTypesFilter(listOf(PlaceTypes.ADDRESS))
                .setSessionToken(token)
                .setQuery(query)
                .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    Log.i(TAG, prediction.placeId)
                    Log.i(TAG, prediction.getPrimaryText(null).toString())
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }

    }


    // [END maps_current_place_get_device_location]

    /**
     * 장치 위치를 사용할 수 있는 권한을 사용자에게 묻습니다.
     */
    // [START maps_current_place_location_permission]
    private fun getLocationPermission() {
        /*
         * 위치 권한을 요청하여 위치를 알 수 있습니다.
         * 장치. 권한 요청의 결과는 콜백에 의해 처리되며,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }
    // [END maps_current_place_location_permission]

    /**
     * 위치 권한 요청 결과를 처리합니다.
     */
    // [START maps_current_place_on_request_permissions_result]
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }
    // [END maps_current_place_on_request_permissions_result]

    /**
     * 가능성이 있는 장소 목록에서 현재 장소를 선택하라는 메시지를 표시하고
     * 지도의 현재 위치 - 사용자가 위치 권한을 부여한 경우.
     */
    // [START maps_current_place_show_current_place]
    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (map == null) {
            return
        }
        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            // 예상되는 장소를 가져옵니다. 즉, 비즈니스 및 기타 관심 장소를
            // 기기의 현재 위치와 가장 잘 일치합니다.
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count = if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
                        likelyPlaces.placeLikelihoods.size
                    } else {
                        M_MAX_ENTRIES
                    }
                    var i = 0
                    likelyPlaceNames = arrayOfNulls(count)
                    likelyPlaceAddresses = arrayOfNulls(count)
                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                    likelyPlaceLatLngs = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // Build a list of likely places to show the user.
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
                        i++
                        if (i > count - 1) {
                            break
                        }
                    }

                    // 사용자에게 가능한 장소 목록을 제공하는 대화 상자를 표시하고
                    // 선택한 장소의 마커.
                    openPlacesDialog()
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                }
            }
        } else {
            // 사용자가 권한을 부여하지 않았습니다.
            Log.i(TAG, "The user did not grant location permission.")

            // 사용자가 장소를 선택하지 않았기 때문에 기본 마커를 추가합니다.
            map?.addMarker(MarkerOptions()
                .title(getString(R.string.default_info_title))
                .position(defaultLocation)
                .snippet(getString(R.string.default_info_snippet)))

            // Prompt the user for permission.
            getLocationPermission()
        }
    }
    // [END maps_current_place_show_current_place]

    /**
     * 사용자가 가능한 장소 목록에서 장소를 선택할 수 있는 양식을 표시합니다.
     */
    // [START maps_current_place_open_places_dialog]
    private fun openPlacesDialog() {
        // 사용자에게 현재 있는 장소를 선택하도록 요청합니다.
        val listener = DialogInterface.OnClickListener { dialog, which -> // The "which" argument contains the position of the selected item.
            val markerLatLng = likelyPlaceLatLngs[which]
            var markerSnippet = likelyPlaceAddresses[which]
            if (likelyPlaceAttributions[which] != null) {
                markerSnippet = """
                    $markerSnippet
                    ${likelyPlaceAttributions[which]}
                    """.trimIndent()
            }

            if (markerLatLng == null) {
                return@OnClickListener
            }

            // 정보 창과 함께 선택한 장소에 대한 마커를 추가합니다.
            // 해당 장소에 대한 정보를 표시합니다.
            map?.addMarker(MarkerOptions()
                .title(likelyPlaceNames[which])
                .position(markerLatLng)
                .snippet(markerSnippet))


            // 마커 위치에 지도의 카메라 위치를 지정합니다.
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                DEFAULT_ZOOM.toFloat()))

            // 추가 정보를 표시하는 버튼을 대화 상자에 추가합니다.
            val alertDialog = AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setMessage(markerSnippet)
                .setPositiveButton(R.string.pick_place) { _,_ ->
                    showPlaceDetails(likelyPlaceNames[which], markerSnippet)
                }
        }

        // 대화 상자를 표시합니다.
        AlertDialog.Builder(this)
            .setTitle(R.string.pick_place)
            .setItems(likelyPlaceNames, listener)
            .show()
    }
    private fun showPlaceDetails(placeName: String?, placeAddress: String?) {
        val detailsDialog = AlertDialog.Builder(this)
            .setTitle(placeName) // 선택한 장소의 이름을 다이얼로그 제목으로 설정
            .setMessage(placeAddress) // 선택한 장소의 주소를 다이얼로그 메시지로 설정
            .setPositiveButton(android.R.string.ok, null)
            .create()

        detailsDialog.show()

        // 특정 프래그먼트로 이동하는 로직
        val fragment = PlaceDetailsFragment.newInstance(placeName,placeAddress)
        supportFragmentManager.beginTransaction()
            .replace(R.id.currentmap_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }
    // [END maps_current_place_open_places_dialog]

    /**
     *    사용자가 위치 권한을 부여했는지 여부에 따라 지도의 UI 설정을 업데이트합니다.
     */
    // [START maps_current_place_update_location_ui]
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // [END maps_current_place_update_location_ui]



    companion object {
        private val TAG = MapsActivityCurrentPlace::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }
}