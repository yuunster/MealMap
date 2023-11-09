package com.bignerdranch.android.nomnommap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.nomnommap.databinding.FragmentMealListBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG = "CrimeListFragment"

class MealListFragment : Fragment() {
    private var _binding: FragmentMealListBinding? = null
    private var job: Job? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val mealListViewModel: MealListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMealListBinding.inflate(layoutInflater, container, false)

        binding.mealRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealListViewModel.meals.collect{ crimes ->
                    binding.mealRecyclerView.adapter =
                        MealListAdapter(crimes) { crimeId ->
                            findNavController().navigate(
                                MealListFragmentDirections.showMealDetail(crimeId)
                            )
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_meal_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_meal -> {
                showNewMeal()
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
            mealListViewModel.addMeal(newMeal)
            findNavController().navigate(
                MealListFragmentDirections.showMealDetail(newMeal.id)
            )
        }
    }
}