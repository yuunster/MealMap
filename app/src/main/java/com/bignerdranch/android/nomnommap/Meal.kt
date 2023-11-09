package com.bignerdranch.android.nomnommap

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Meal(
    @PrimaryKey val id: UUID,
    val title: String,
    val date: Date,
    val description: String = "",
    val photoFileName: String? = null
)
