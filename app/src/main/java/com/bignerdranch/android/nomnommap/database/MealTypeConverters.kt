package com.bignerdranch.android.nomnommap.database

import androidx.room.TypeConverter
import java.util.Date

class MealTypeConverters {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }
    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }
}