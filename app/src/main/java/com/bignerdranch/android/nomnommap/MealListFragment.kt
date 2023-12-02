package com.bignerdranch.android.nomnommap

import android.os.Bundle
import android.view.LayoutInflater
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
import kotlinx.coroutines.launch

private const val TAG = "MealListFragment"

class MealListFragment : Fragment() {
    private var _binding: FragmentMealListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val mealListViewModel: MealListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealListBinding.inflate(layoutInflater, container, false)

        binding.mealRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealListViewModel.meals.collect{ meals ->
                    binding.mealRecyclerView.adapter =
                        context?.let {
                            MealListAdapter(meals, it, MainActivity().loadSettings()) { mealId ->
                                findNavController().navigate(
                                    MealListFragmentDirections.showMealDetail(mealId)
                                )
                            }
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}