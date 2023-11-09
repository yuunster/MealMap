package com.bignerdranch.android.nomnommap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class MealDetailViewModel(mealId: UUID) : ViewModel() {
    private val mealRepository = MealRepository.get()
    private val _meal: MutableStateFlow<Meal?> = MutableStateFlow(null)
    val meal: StateFlow<Meal?> = _meal.asStateFlow()

    init {
        viewModelScope.launch {
            _meal.value = mealRepository.getMeal(mealId)
        }
    }

    fun updateMeal(onUpdate: (Meal) -> Meal) {
        _meal.update { oldMeal ->
            oldMeal?.let { onUpdate(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        meal.value?.let { mealRepository.updateMeal(it) }
    }
}
class MealDetailViewModelFactory(
    private val mealId: UUID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MealDetailViewModel(mealId) as T
    }
}