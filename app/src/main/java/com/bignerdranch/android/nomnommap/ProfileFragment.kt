package com.bignerdranch.android.nomnommap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bignerdranch.android.nomnommap.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentProfileBinding? = null
    private val profileViewModel: ProfileViewModel by viewModels()
    private val dailyCalories = 2000
    private val dailyProteins = 55
    private val dailyCarbs = 1000
    private val dailyFats = 300
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        _binding =
            FragmentProfileBinding.inflate(layoutInflater, container, false)
        binding.apply {
            curCalories.max = dailyCalories
            curProteins.max = dailyProteins
            curCarbs.max = dailyCarbs
            curFats.max = dailyFats
        }

        calculateDaily()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            btnLogout.setOnClickListener {
                auth.signOut()
                val intent = Intent(activity?.applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        }

    }

    private fun calculateDaily() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.meals.collect{ meals ->
                    val currentDate = Date()
                    val formattedDate = SimpleDateFormat("MMM dd yyyy", Locale.US)
                    val today = formattedDate.format(currentDate).toString()
                    var curCalories = 0
                    var curProteins = 0
                    var curCarbs = 0
                    var curFats = 0

                    for (meal in meals) {
                        if (formattedDate.format(meal.date).toString() == today) {
                            curCalories += meal.calories.toIntOrNull() ?: 0
                            curProteins += meal.proteins.toIntOrNull() ?: 0
                            curCarbs += meal.carbs.toIntOrNull() ?: 0
                            curFats += meal.fats.toIntOrNull() ?: 0
                        }
                    }

                    binding.curCalories.progress = curCalories
                    binding.curProteins.progress = (curProteins)
                    binding.curCarbs.progress = (curCarbs)
                    binding.curFats.progress = (curFats)
                }
            }
        }
    }
}