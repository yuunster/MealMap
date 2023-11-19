package com.bignerdranch.android.nomnommap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.nomnommap.databinding.FragmentMealDetailBinding
import kotlinx.coroutines.launch

class MealDetailFragment : Fragment() {
    private var _binding: FragmentMealDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val args: MealDetailFragmentArgs by navArgs()

    private val mealDetailViewModel: MealDetailViewModel by viewModels {
        MealDetailViewModelFactory(args.mealId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentMealDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(meal: Meal) {
        binding.apply {
            if (editMealName.text.toString() != meal.title) {
                editMealName.setText(meal.title)
            }
            if (editCalories.text.toString() != meal.calories.toString()) {
                meal.calories.let { editCalories.setText(it) }
            }
            if (editProteins.text.toString() != meal.proteins.toString()) {
                meal.proteins.let { editProteins.setText(it) }
            }
            if (editCarbs.text.toString() != meal.carbs.toString()) {
                meal.carbs.let { editCarbs.setText(it) }
            }
            if (editFats.text.toString() != meal.fats.toString()) {
                meal.fats.let { editFats.setText(it) }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            editMealName.doOnTextChanged { text, _, _, _ ->
                mealDetailViewModel.updateMeal { oldMeal ->
                    oldMeal.copy(title = text.toString())
                }
            }
            editCalories.doOnTextChanged { text, _, _, _ ->
                mealDetailViewModel.updateMeal { oldMeal ->
                    oldMeal.copy(calories = text.toString())
                }
            }
            editProteins.doOnTextChanged { text, _, _, _ ->
                mealDetailViewModel.updateMeal { oldMeal ->
                    oldMeal.copy(proteins = text.toString())
                }
            }
            editCarbs.doOnTextChanged { text, _, _, _ ->
                mealDetailViewModel.updateMeal { oldMeal ->
                    oldMeal.copy(carbs = text.toString())
                }
            }
            editFats.doOnTextChanged { text, _, _, _ ->
                mealDetailViewModel.updateMeal { oldMeal ->
                    oldMeal.copy(fats = text.toString())
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealDetailViewModel.meal.collect { meal ->
                    meal?.let { updateUi(it) }
                }
            }
        }
    }
}