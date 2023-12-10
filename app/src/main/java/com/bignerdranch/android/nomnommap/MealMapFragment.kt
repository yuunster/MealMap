package com.bignerdranch.android.nomnommap

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.Date
import java.util.UUID


class MealMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener{
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
    ): View {
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
            lifecycleScope.launch {
                val location = async { updateLocation() }.await()!!
                showNewMeal(location)
                //print latitude and longitude to console
                Log.d("TEST", "${currentLocation.latitude} and ${currentLocation.longitude}")
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        map.let{
            googleMap = it
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(this)

        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMyLocationClickListener(this)

        enableLocation()
        setUpMap()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            enableLocation()
        }
        googleMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this.requireActivity()) { location ->
            if (location != null){
                currentLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                //placeMarkerOnMap(currentLatLong)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
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

    private suspend fun updateLocation() : Location? {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        }
        return try {
            return fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        googleMap.isMyLocationEnabled = true
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(requireContext(), "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(requireContext(), "Current location:\n${location.latitude}, ${location.longitude}", Toast.LENGTH_LONG)
            .show()
    }


    private fun placeMarkerOnMap(currentLatLong: LatLng, name: String) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title(name)
        googleMap.addMarker(markerOptions)
    }

    private fun showNewMeal(location: Location) {
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
                latitude = location.latitude,
                longitude = location.longitude
            )
            mealMapViewModel.addMeal(newMeal)
            findNavController().navigate(
                MealMapFragmentDirections.showMealDetail(newMeal.id)
            )
        }
    }

    override fun onMarkerClick(p0: Marker) = false
}