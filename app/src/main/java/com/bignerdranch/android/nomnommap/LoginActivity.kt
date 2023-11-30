package com.bignerdranch.android.nomnommap

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.nomnommap.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var _binding: ActivityLoginBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    public override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(supportActionBar!=null)
            this.supportActionBar?.hide();
        _binding =
            ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent != null) {
            binding.email.setText(intent.getStringExtra("email"))
            binding.password.setText(intent.getStringExtra("password"))
        }

        binding.registerNow.setOnClickListener{
            val intent = Intent(applicationContext, RegisterActivity::class.java)
            intent.putExtra("email", binding.email.text.toString())
            intent.putExtra("password", binding.password.text.toString())
            startActivity(intent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@LoginActivity, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@LoginActivity, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(
                            this@LoginActivity,
                            "Login Successful",
                            Toast.LENGTH_SHORT,
                        ).show()
                        val user = auth.currentUser
                        val intent = Intent(applicationContext,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            this@LoginActivity,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
    }
}