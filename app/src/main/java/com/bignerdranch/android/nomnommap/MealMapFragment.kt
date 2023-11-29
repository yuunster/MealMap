package com.bignerdranch.android.nomnommap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.nomnommap.databinding.FragmentMealMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID


class MealMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var _binding: FragmentMealMapBinding? = null
    private val mealMapViewModel: MealMapViewModel by viewModels()
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentMealMapBinding.inflate(layoutInflater, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.map, mapFragment!!)
            ?.commit()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logMeal.setOnClickListener {
            showNewMeal()

            //print latitude and longitude to console
            Log.d("TEST", "${currentLocation.latitude} and ${currentLocation.longitude}")
        }
    }

    override fun onMapReady(map: GoogleMap) {
        map.let{
            googleMap = it
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        googleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this.requireActivity()) { location ->
            if (location != null){
                currentLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLong)
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }
        }

        //Creates a coroutine and places markers on map for each meal in database
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealMapViewModel.meals.collect{ meals ->
                    for (meal in meals) {
                        val tempLatLng = LatLng(meal.latitude, meal.longitude)
                        placeMarkerOnMap(tempLatLng, meal.title)
                    }
                }
            }
        }
    }

    private fun placeMarkerOnMap(currentLatLong: LatLng, name: String) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title(name)
        googleMap.addMarker(markerOptions)
    }

    private fun showNewMeal() {
        Log.d("TEST", "${currentLocation.latitude} and ${currentLocation.longitude}")
        viewLifecycleOwner.lifecycleScope.launch {
            val newMeal = Meal(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                calories = "",
                proteins = "",
                carbs = "",
                fats = "",
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
            mealMapViewModel.addMeal(newMeal)
            findNavController().navigate(
                MealMapFragmentDirections.showMealDetail(newMeal.id)
            )
        }
    }

    override fun onMarkerClick(p0: Marker) = false
}