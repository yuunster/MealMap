package com.bignerdranch.android.nomnommap

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import java.util.UUID

@Entity
data class Meal(
    @PrimaryKey val id: UUID,
    val title: String,
    val date: Date,
    val calories: String,
    val proteins: String,
    val carbs: String,
    val fats: String,
    val latitude: Double,
    val longitude: Double
    val photoFileName: String? = null
)
