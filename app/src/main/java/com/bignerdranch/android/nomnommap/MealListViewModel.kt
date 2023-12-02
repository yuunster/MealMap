package com.bignerdranch.android.nomnommap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "MealListViewModel"

class MealListViewModel : ViewModel() {
    private val mealRepository = MealRepository.get()

    private val _meals: MutableStateFlow<List<Meal>> = MutableStateFlow(emptyList())
    val meals: StateFlow<List<Meal>>
        get() = _meals.asStateFlow()

    init {
        viewModelScope.launch {
            mealRepository.getMeals().collect {
                _meals.value = it
            }
        }
    }
}