package com.bignerdranch.android.nomnommap

import android.content.Context
import androidx.room.Room
import com.bignerdranch.android.nomnommap.database.MealDatabase
import com.bignerdranch.android.nomnommap.database.migration_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val DATABASE_NAME = "meal-database"

class MealRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
    ) {
    private val database: MealDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            MealDatabase::class.java,
            DATABASE_NAME
        )
        .addMigrations(migration_1_2)
        .build()

    fun getMeals(): Flow<List<Meal>>
        = database.mealDao().getMeals()
    suspend fun getMeal(id: UUID): Meal = withContext(Dispatchers.IO) { database.mealDao().getMeal(id) }

    fun updateMeal(meal: Meal) {
        coroutineScope.launch {
            database.mealDao().updateMeal(meal)
        }
    }

    suspend fun addMeal(meal: Meal) {
        database.mealDao().addMeal(meal)
    }

    companion object {
        private var INSTANCE: MealRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = MealRepository(context)
            }
        }
        fun get(): MealRepository {
            return INSTANCE ?:
            throw IllegalStateException("MealRepository must be initialized")
        }
    }
}