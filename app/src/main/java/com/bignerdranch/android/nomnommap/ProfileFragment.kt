package com.bignerdranch.android.nomnommap

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.nomnommap.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private var auth: FirebaseAuth = Firebase.auth
    private var _binding: FragmentProfileBinding? = null
    private val profileViewModel: ProfileViewModel by viewModels()
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableLocation()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentProfileBinding.inflate(layoutInflater, container, false)
        binding.loading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            loadSettings()
            calculateDaily()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnSettings.setOnClickListener {
                findNavController().navigate(
                    ProfileFragmentDirections.showSettings()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadSettings() {
        val db = Firebase.firestore
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.apply {
                        curCalories.max = document.get("calories").toString().toIntOrNull() ?: 0
                        curProteins.max = document.get("proteins").toString().toIntOrNull() ?: 0
                        curCarbs.max = document.get("carbs").toString().toIntOrNull() ?: 0
                        curFats.max = document.get("fats").toString().toIntOrNull() ?: 0
                        loading.visibility = View.GONE
                    }
                }
            }
    }

    private fun calculateDaily() {
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.meals.collect { meals ->
                val currentDate = Date()
                val formattedDate = SimpleDateFormat("MMM dd yyyy", Locale.US)
                val today = formattedDate.format(currentDate).toString()

                for (meal in meals) {
                    if (formattedDate.format(meal.date).toString() == today) {
                        binding.apply {
                            curCalories.incrementProgressBy(meal.calories.toIntOrNull() ?: 0)
                            curProteins.incrementProgressBy(meal.proteins.toIntOrNull() ?: 0)
                            curCarbs.incrementProgressBy(meal.carbs.toIntOrNull() ?: 0)
                            curFats.incrementProgressBy(meal.fats.toIntOrNull() ?: 0)
                        }

                    }
                }
            }
        }
    }

    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ProfileFragment.LOCATION_REQUEST_CODE
            )
            return
        }
    }

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }
}