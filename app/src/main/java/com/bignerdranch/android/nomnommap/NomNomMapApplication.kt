package com.bignerdranch.android.nomnommap

import android.app.Application

class NomNomMapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MealRepository.initialize(this)
    }
}