package com.bignerdranch.android.nomnommap.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bignerdranch.android.nomnommap.Meal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface MealDao {
    @Query("SELECT * FROM meal")
    fun getMeals(): Flow<List<Meal>>
    @Query("SELECT * FROM meal WHERE id=(:id)")
    suspend fun getMeal(id: UUID): Meal
    @Update
    suspend fun updateMeal(meal: Meal)
    @Insert
    suspend fun addMeal(meal: Meal)
}
