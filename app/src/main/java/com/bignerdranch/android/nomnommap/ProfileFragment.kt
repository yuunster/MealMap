package com.bignerdranch.android.nomnommap

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.nomnommap.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentProfileBinding? = null
    private val profileViewModel: ProfileViewModel by viewModels()
    private var storage = Firebase.storage
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
            FragmentProfileBinding.inflate(layoutInflater, container, false)

        updatePhoto()

        // Load settings from MainActivity and calculate dailies
        binding.loading.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch() {
            settings = MainActivity().loadSettings()
            binding.apply {
                userName.text = settings.username
                curCalories.max = settings.calories.toIntOrNull() ?: 0
                curProteins.max = settings.proteins.toIntOrNull() ?: 0
                curCarbs.max = settings.carbs.toIntOrNull() ?: 0
                curFats.max = settings.fats.toIntOrNull() ?: 0
                binding.loading.visibility = View.GONE
            }
        }
        calculateDaily()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnSettings.setOnClickListener {
                findNavController().navigate(
                    ProfileFragmentDirections.showSettings()
                )
            }

            val captureImageIntent = takePhoto.contract.createIntent(
                requireContext(),
                Uri.EMPTY
            )

            profilePic.setOnClickListener {
                if (canResolveIntent(captureImageIntent)) {
                    photoName = "Profile_Pic.JPG"
                    val photoFile = File(requireContext().applicationContext.filesDir,
                        photoName)
                    val photoUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.bignerdranch.android.nomnommap.fileprovider",
                        photoFile
                    )
                    takePhoto.launch(photoUri)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            updatePhoto()
            val storageRef = storage.reference
            val file = Uri.fromFile(photoName?.let {
                File(requireContext().applicationContext.filesDir,
                    it
                )
            })
            val profilePicRef = storageRef.child("users/${auth.currentUser?.uid.toString()}/profilePic")
            val uploadTask = profilePicRef.putFile(file)
            var downloadUri: Uri? = null
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                profilePicRef.downloadUrl
            }.addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                } else {
                    Toast.makeText(activity, "Failed to get downloadUri", Toast.LENGTH_SHORT).show()
                }
            }
            val update = UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build()
            auth.currentUser?.updateProfile(update)

            uploadTask.addOnFailureListener {
                Toast.makeText(activity, "Failed profile pic upload", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private var photoName: String? = null

    private fun updatePhoto() {
        val photoFile = File(requireContext().applicationContext.filesDir, "Profile_Pic.JPG")

        if (photoFile.exists()) {
            binding.profilePic.doOnLayout { measuredView ->
                val scaledBitmap = getScaledBitmap(
                    photoFile.path,
                    measuredView.width,
                    measuredView.height
                )
                binding.profilePic.setImageBitmap(scaledBitmap)
                binding.profilePic.tag = "Profile_Pic.JPG"
            }
        } else {
            //TODO: FETCH PROFILE PIC FROM auth.currentUser.photoUrl
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

    private fun calculateDaily() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            profileViewModel.meals.collect { meals ->
                val currentDate = Date()
                val formattedDate = SimpleDateFormat("MMM dd yyyy", Locale.US)
                val today = formattedDate.format(currentDate).toString()

                for (meal in meals) {
                    if (formattedDate.format(meal.date).toString() == today) {
                        binding.apply {
                            curCalories.incrementProgressBy(meal.calories.toIntOrNull() ?: 0)
                            curProteins.incrementProgressBy(meal.proteins.toIntOrNull() ?: 0)
                            curCarbs.incrementProgressBy(meal.carbs.toIntOrNull() ?: 0)
                            curFats.incrementProgressBy(meal.fats.toIntOrNull() ?: 0)
                        }
                    }
                }
            }
        }
    }

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }
}