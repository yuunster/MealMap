package com.bignerdranch.android.nomnommap

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.nomnommap.databinding.ListItemMealBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class MealHolder(
    private val binding: ListItemMealBinding,
    private val context: Context,
    private val settings: Settings
) : RecyclerView.ViewHolder(binding.root){
    fun bind(meal: Meal, onMealClicked: (mealId: UUID) -> Unit) {
        binding.mealTitle.text = meal.title
        val formattedDate = SimpleDateFormat("EE, MMM dd, yyyy", Locale.US)
        val formattedTime = SimpleDateFormat("hh:mm a", Locale.US)
        binding.mealDate.text = formattedDate.format(meal.date).toString()
        binding.mealTime.text = formattedTime.format(meal.date).toString()
        binding.calories.max = settings.calories.toIntOrNull() ?: 100
        binding.proteins.max = settings.proteins.toIntOrNull() ?: 100
        binding.carbs.max = settings.carbs.toIntOrNull() ?: 100
        binding.fats.max = settings.fats.toIntOrNull() ?: 100
        Log.d("TEST", "setings calories: ${settings.calories.toIntOrNull()}")
        Log.d("TEST", "increment calories: ${meal.calories.toIntOrNull()}")
        binding.calories.incrementProgressBy(meal.calories.toIntOrNull() ?: 0)
        binding.proteins.incrementProgressBy(meal.proteins.toIntOrNull() ?: 0)
        binding.carbs.incrementProgressBy(meal.carbs.toIntOrNull() ?: 0)
        binding.fats.incrementProgressBy(meal.fats.toIntOrNull() ?: 0)
        updatePhoto(meal.photoFileName)

        binding.root.setOnClickListener {
            onMealClicked(meal.id)
        }
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.mealPhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(context.applicationContext.filesDir, it)
            }
            if (photoFile?.exists() == true) {
                binding.mealPhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.mealPhoto.setImageBitmap(scaledBitmap)
                    binding.mealPhoto.tag = photoFileName
                }
            } else {
                binding.mealPhoto.setImageBitmap(null)
                binding.mealPhoto.tag = null
            }
        }
    }
}

class MealListAdapter(
    private val meals: List<Meal>,
    private val context: Context,
    private val settings: Settings,
    private val onMealClicked: (mealId: UUID) -> Unit
) : RecyclerView.Adapter<MealHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MealHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMealBinding.inflate(inflater, parent, false)
        return MealHolder(binding, context, settings)
    }

    override fun onBindViewHolder(holder: MealHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal, onMealClicked)
    }

    override fun getItemCount() = meals.size
}
