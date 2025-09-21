package com.example.xamu_wil_project.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.xamu_wil_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.xamu_wil_project.ui.DashboardActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etUsername: EditText = findViewById(R.id.etUsernameRegister)
        val etFirst: EditText = findViewById(R.id.etFirstName)
        val etLast: EditText = findViewById(R.id.etLastName)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etQualification: EditText = findViewById(R.id.etQualification)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val etConfirm: EditText = findViewById(R.id.etConfirmPassword)

        val btnSignup: Button = findViewById(R.id.btnSignup)
        val btnCancel: Button = findViewById(R.id.btnCancelSignup)

        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val first = etFirst.text.toString().trim()
            val last = etLast.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val qual = etQualification.text.toString().trim()
            val pass = etPassword.text.toString()
            val confirm = etConfirm.text.toString()

            if (username.isEmpty() || first.isEmpty() || last.isEmpty() ||
                email.isEmpty() || qual.isEmpty() || pass.isEmpty() || confirm.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userMap = mapOf(
                        "username" to username,
                        "firstname" to first,
                        "lastname" to last,
                        "email" to email,
                        "qualification" to qual
                    )
                    FirebaseDatabase.getInstance().getReference("AppUsers")
                        .child(uid)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile created", Toast.LENGTH_SHORT).show()
                            // After profile creation, go to the app dashboard (central hub)
                            startActivity(Intent(this, DashboardActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, e.message ?: "Failed to save profile", Toast.LENGTH_LONG).show()
                        }
                } else {
                    val ex = task.exception
                    val msg = ex?.localizedMessage ?: "Sign up failed"
                    Log.e(TAG, "Sign-up failed", ex)
                    // Friendly guidance for server-side reCAPTCHA/App Check misconfigurations
                    if (msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true)
                        || msg.contains("reCAPTCHA", ignoreCase = true)
                        || msg.contains("App Check", ignoreCase = true)
                    ) {
                        // If the App Check API is not enabled you'll often see a 403 with the API URL in the message.
                        val apiEnableUrl = "https://console.developers.google.com/apis/api/firebaseappcheck.googleapis.com/overview?project=722862111675"
                        val builder = AlertDialog.Builder(this)
                            .setTitle("Sign-up blocked by server configuration")
                            .setMessage(
                                "Sign-up is blocked by the server's abuse-protection configuration (reCAPTCHA/App Check).\n\n" +
                                        "To fix this in the Firebase/Google Cloud console:\n" +
                                        "1) Enable the Firebase App Check API for your project.\n" +
                                        "2) Register the App Check debug token (for development) under App Check.\n" +
                                        "3) Configure reCAPTCHA for email sign-ups or disable the enforcement for testing.\n\n" +
                                        "Error: $msg"
                            )
                            .setPositiveButton("Open API in Cloud Console") { _, _ ->
                                try {
                                    startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(apiEnableUrl))
                                    )
                                } catch (_: Exception) { /* ignore */ }
                            }
                            .setNeutralButton("Open Firebase Console") { _, _ ->
                                try {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://console.firebase.google.com/project/722862111675/overview")
                                        )
                                    )
                                } catch (_: Exception) { /* ignore */ }
                            }
                            .setNegativeButton("OK", null)
                        builder.show()
                    } else {
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        btnCancel.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
