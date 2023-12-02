package com.bignerdranch.android.nomnommap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private var auth = Firebase.auth
    private var settings = Settings()
    private var job: Job?= null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(supportActionBar!=null)
            this.supportActionBar?.hide();

        GlobalScope.launch(Dispatchers.IO) {
            loadSettings()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        navView.setupWithNavController(navController)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun loadSettings() : Settings {
        if (job == null) {
            settings.username = auth.currentUser?.displayName.toString()
            val db = Firebase.firestore
            job = GlobalScope.launch(Dispatchers.IO) {
                val docRef = db.collection("users").document(auth.uid.toString())
                settings = docRef.get().await().toObject(Settings::class.java)!!
            }
        }

        job!!.join()
        return settings
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setSettings(settings: Settings) {
        this.settings = settings
        val update = UserProfileChangeRequest.Builder()
            .setDisplayName(settings.username)
            .build()
        val db = Firebase.firestore

        job = GlobalScope.launch(Dispatchers.IO) {
            auth.currentUser?.updateProfile(update)
            db.collection("users").document(auth.uid.toString())
                .set(settings)
        }
    }
}