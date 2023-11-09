package com.bignerdranch.android.nomnommap.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.nomnommap.Meal

@Database(entities = [ Meal::class ], version=1)
@TypeConverters(MealTypeConverters::class)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
