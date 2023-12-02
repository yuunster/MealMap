package com.bignerdranch.android.nomnommap

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.nomnommap.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var _binding: ActivityRegisterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(supportActionBar!=null)
            this.supportActionBar?.hide();
        _binding =
            ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val intentLoginActivity = Intent(this, LoginActivity::class.java)

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Check if received intent from other activities
        if (intent != null) {
            binding.email.setText(intent.getStringExtra("email"))
            binding.password.setText(intent.getStringExtra("password"))
        }

        binding.loginNow.setOnClickListener{
            intentLoginActivity.putExtra("email", binding.email.text.toString())
            intentLoginActivity.putExtra("password", binding.password.text.toString())
            startActivity(intentLoginActivity)
        }

        binding.btnRegister.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val username = binding.userName.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@RegisterActivity, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@RegisterActivity, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@RegisterActivity, "Enter user name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                        auth.currentUser?.updateProfile(update)
                            ?.addOnSuccessListener {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Account Created.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                            ?.addOnFailureListener {
                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Failed to update display name",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        // Sign in success, update UI with the signed-in user's information
                        intentLoginActivity.putExtra("email", binding.email.text)
                        intentLoginActivity.putExtra("password", binding.password.text)
                        startActivity(intentLoginActivity)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this@RegisterActivity,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
}