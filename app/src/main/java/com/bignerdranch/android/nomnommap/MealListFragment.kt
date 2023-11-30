package com.bignerdranch.android.nomnommap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.nomnommap.databinding.FragmentMealListBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG = "MealListFragment"

class MealListFragment : Fragment() {
    private var _binding: FragmentMealListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val mealListViewModel: MealListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMealListBinding.inflate(layoutInflater, container, false)

        binding.mealRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealListViewModel.meals.collect{ meals ->
                    binding.mealRecyclerView.adapter =
                        context?.let {
                            val settings = Settings()

                            val auth = Firebase.auth
                            val db = Firebase.firestore
                            val docRef = db.collection("users").document(auth.uid.toString())
                            coroutineScope {
                                val deferred = async {
                                 docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        settings.calories = document.get("calories")?.toString() ?: ""
                                        settings.proteins = document.get("proteins")?.toString() ?: ""
                                        settings.carbs = document.get("carbs")?.toString() ?: ""
                                        settings.fats = document.get("fats")?.toString() ?: ""
                                    }
                                }
                            }
                                deferred.await()
                            }

                            MealListAdapter(meals, it, settings) { mealId ->
                                findNavController().navigate(
                                    MealListFragmentDirections.showMealDetail(mealId)
                                )
                            }
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}