package com.bignerdranch.android.nomnommap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.nomnommap.databinding.ListItemMealBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class MealHolder(
    private val binding: ListItemMealBinding
) : RecyclerView.ViewHolder(binding.root){
    fun bind(meal: Meal, onMealClicked: (mealId: UUID) -> Unit) {
        binding.mealTitle.text = meal.title
        val formattedDate = SimpleDateFormat("EE, MMM dd, yyyy", Locale.US)
        val formattedTime = SimpleDateFormat("hh:mm a", Locale.US)
        binding.mealDate.text = formattedDate.format(meal.date).toString()
        binding.mealTime.text = formattedTime.format(meal.date).toString()
        binding.mealPhoto

        binding.root.setOnClickListener {
            onMealClicked(meal.id)
        }
    }
}

class MealListAdapter(
    private val meals: List<Meal>,
    private val onMealClicked: (mealId: UUID) -> Unit
) : RecyclerView.Adapter<MealHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MealHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMealBinding.inflate(inflater, parent, false)
        return MealHolder(binding)
    }

    override fun onBindViewHolder(holder: MealHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal, onMealClicked)
    }

    override fun getItemCount() = meals.size
}
