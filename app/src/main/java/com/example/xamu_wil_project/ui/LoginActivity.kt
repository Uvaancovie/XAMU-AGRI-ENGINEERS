package com.example.xamu_wil_project.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.example.xamu_wil_project.ui.RegisterActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.xamu_wil_project.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var root: View
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoRegister: Button
    private lateinit var progress: View

    private val TAG = "LoginActivity"
    private val PROFILE_TIMEOUT_MS = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Auth (no KTX, no extension functions)
        auth = FirebaseAuth.getInstance()

        // Views
        root = findViewById(R.id.root)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)
        progress = findViewById(R.id.progress)

        btnLogin.setOnClickListener { doLogin() }

        // Launch RegisterActivity when Create account is tapped
        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        // If user is signed in, verify profile exists in RTDB; otherwise route to RegisterActivity
        val user = auth.currentUser
        if (user == null) return

        Log.d(TAG, "User signed in, checking profile: ${user.uid}")
        checkProfileAndNavigate(user.uid)
    }

    private fun doLogin() {
        val email = etEmail.text?.toString()?.trim().orEmpty()
        val password = etPassword.text?.toString()?.trim().orEmpty()

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(root, "Enter email & password", Snackbar.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    // After sign-in, ensure profile exists before showing home
                    val user = auth.currentUser
                    if (user == null) {
                        Snackbar.make(root, "Signed in but no user object", Snackbar.LENGTH_LONG).show()
                        return@addOnCompleteListener
                    }
                    Log.d(TAG, "Signed in: checking profile ${user.uid}")
                    checkProfileAndNavigate(user.uid)
                } else {
                    val msg = task.exception?.localizedMessage ?: "Login failed"
                    Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show()
                }
            }
    }

    private fun checkProfileAndNavigate(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("AppUsers/$uid")
        var handled = false
        val handler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!handled) {
                handled = true
                Log.w(TAG, "Profile read timed out; proceeding to home")
                // Proceed silently on timeout to avoid noisy toasts during demos
                goToHome()
            }
        }
        handler.postDelayed(timeoutRunnable, PROFILE_TIMEOUT_MS)

        ref.get().addOnSuccessListener { snap ->
            if (handled) return@addOnSuccessListener
            handled = true
            handler.removeCallbacks(timeoutRunnable)
            if (snap.exists()) {
                Log.d(TAG, "Profile exists, navigating to home")
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
                goToHome()
            } else {
                Log.d(TAG, "Profile missing, navigating to RegisterActivity")
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }.addOnFailureListener { ex ->
            if (handled) return@addOnFailureListener
            handled = true
            handler.removeCallbacks(timeoutRunnable)
            Log.e(TAG, "Failed to read profile, proceeding to home", ex)
            // Fail-open silently; the dashboard will still appear for demos
            goToHome()
        }
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        btnGoRegister.isEnabled = !loading
        etEmail.isEnabled = !loading
        etPassword.isEnabled = !loading
    }

    private fun goToHome() {
        // Landing screen after sign-in: DashboardActivity central hub
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
