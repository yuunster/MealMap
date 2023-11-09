package com.bignerdranch.android.nomnommap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.nomnommap.databinding.FragmentMealDetailBinding
import com.bignerdranch.android.nomnommap.databinding.FragmentMealMapBinding
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.UUID

class MealMapFragment : Fragment() {
    private var _binding: FragmentMealMapBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val mealRepository = MealRepository.get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentMealMapBinding.inflate(layoutInflater, container, false)
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
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_meal_map, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.show_meal_list -> {
                findNavController().navigate(
                    MealMapFragmentDirections.showMealList()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNewMeal() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newMeal = Meal(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                description = ""
            )
            mealRepository.addMeal(newMeal)
            findNavController().navigate(
                MealMapFragmentDirections.showMealDetail(newMeal.id)
            )
        }
    }
}