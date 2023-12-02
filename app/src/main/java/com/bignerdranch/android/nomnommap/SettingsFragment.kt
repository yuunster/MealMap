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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentSettingsBinding? = null
    private lateinit var settings: Settings
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
    ): View {
        _binding =
            FragmentSettingsBinding.inflate(layoutInflater, container, false)
        binding.loading.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch() {
            settings = MainActivity().loadSettings()
            binding.apply {
                editUsername.setText(settings.username)
                editCalories.setText(settings.calories)
                editProteins.setText(settings.proteins)
                editCarbs.setText(settings.carbs)
                editFats.setText(settings.fats)
            }
            binding.loading.visibility = View.GONE
        }

        return binding.root
    }

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
                saveSettings()
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

            editUsername.doOnTextChanged { text, _, _, _ ->
                settings.username = text.toString()
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
        MainActivity().setSettings(this.settings)
    }
}