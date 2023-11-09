package com.bignerdranch.android.nomnommap

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bignerdranch.android.nomnommap.databinding.FragmentMealDetailBinding
import getScaledBitmap
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MealDetailFragment : Fragment() {
    private var _binding: FragmentMealDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val args: MealDetailFragmentArgs by navArgs()

    private val mealDetailViewModel: MealDetailViewModel by viewModels {
        MealDetailViewModelFactory(args.mealId)
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            mealDetailViewModel.updateMeal { oldMeal ->
                oldMeal.copy(photoFileName = photoName)
            }
        }
    }

    private var photoName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentMealDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(meal: Meal) {
        binding.apply {
            if (mealTitle.text.toString() != meal.title) {
                mealTitle.setText(meal.title)
            }
            val formattedDate = SimpleDateFormat("EE, MMM dd, yyyy", Locale.US)
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.US)
            binding.mealDate.text = formattedDate.format(meal.date).toString()
            binding.mealTime.text = formattedTime.format(meal.date).toString()
            mealDate.setOnClickListener {
                findNavController().navigate(
                    MealDetailFragmentDirections.selectDate(meal.date)
                )
            }
            mealTime.setOnClickListener {
                findNavController().navigate(
                    MealDetailFragmentDirections.selectTime(meal.date)
                )
            }

            updatePhoto(meal.photoFileName)
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.detailMealPhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }
            if (photoFile?.exists() == true) {
                binding.detailMealPhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.detailMealPhoto.setImageBitmap(scaledBitmap)
                    binding.detailMealPhoto.tag = photoFileName
                }
            } else {
                binding.detailMealPhoto.setImageBitmap(null)
                binding.detailMealPhoto.tag = null
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            mealTitle.doOnTextChanged { text, _, _, _ ->
                mealDetailViewModel.updateMeal { oldMeal ->
                    oldMeal.copy(title = text.toString())
                }
            }

            mealCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG"
                val photoFile = File(requireContext().applicationContext.filesDir,
                    photoName)
                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }

            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                null
            )
            mealCamera.isEnabled = canResolveIntent(captureImageIntent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mealDetailViewModel.meal.collect { meal ->
                    meal?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            mealDetailViewModel.updateMeal { it.copy(date = newDate) }
        }

        setFragmentResultListener(
            TimePickerFragment.REQUEST_KEY_TIME
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(TimePickerFragment.BUNDLE_KEY_TIME) as Date
            mealDetailViewModel.updateMeal { it.copy(date = newDate) }
        }
    }
}