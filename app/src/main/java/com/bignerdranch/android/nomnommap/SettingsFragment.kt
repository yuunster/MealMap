package com.bignerdranch.android.nomnommap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.nomnommap.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSettingsBinding? = null
    private var settings = Settings()
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentSettingsBinding.inflate(layoutInflater, container, false)
        binding.loading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            loadSettings()
        }
        return binding.root
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            clearButton.setOnClickListener {
                editCalories.setText("")
                editProteins.setText("")
                editCarbs.setText("")
                editFats.setText("")
            }

            btnSave.setOnClickListener {
                GlobalScope.launch {
                    saveSettings()
                }
                findNavController().navigate(
                    SettingsFragmentDirections.showProfile()
                )
            }

            btnLogout.setOnClickListener {
                auth.signOut()
                val intent = Intent(activity?.applicationContext, LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
            }

            editCalories.doOnTextChanged { text, _, _, _ ->
                settings.calories = text.toString()
            }
            editProteins.doOnTextChanged { text, _, _, _ ->
                settings.proteins = text.toString()
            }
            editCarbs.doOnTextChanged { text, _, _, _ ->
                settings.carbs = text.toString()
            }
            editFats.doOnTextChanged { text, _, _, _ ->
                settings.fats = text.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveSettings() {
        val update = UserProfileChangeRequest.Builder()
            .setDisplayName(binding.editProfileName.text.toString())
            .build()
        auth.currentUser?.updateProfile(update)

        val db = Firebase.firestore
        db.collection("users").document(auth.uid.toString())
            .set(settings)
    }
    private fun loadSettings() {
        binding.editProfileName.setText(auth.currentUser?.displayName ?: "")

        val db = Firebase.firestore
        val docRef = db.collection("users").document(auth.uid.toString())
        docRef.get()
            .addOnCompleteListener {
                binding.loading.visibility = View.GONE
            }
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.apply {
                        editCalories.setText(document.get("calories")?.toString() ?: "")
                        editProteins.setText(document.get("proteins")?.toString() ?: "")
                        editCarbs.setText(document.get("carbs")?.toString() ?: "")
                        editFats.setText(document.get("fats")?.toString() ?: "")
                    }
                }
            }
    }
}