package com.example.xamu_wil_project.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.example.xamu_wil_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val uid = auth.currentUser?.uid
        val settingsRef = FirebaseDatabase.getInstance()
            .getReference("UserSettings/${uid ?: "anonymous"}/AppSettings")

        val switchDark: SwitchCompat = findViewById(R.id.swtichDarkLightMode)
        val spinner: Spinner = findViewById(R.id.spnChangeLanguage)
        val btnSave: Button = findViewById(R.id.btnSaveSettings)
        val btnCancel: Button = findViewById(R.id.btnCancel)

        val adapter = ArrayAdapter.createFromResource(
            this, R.array.languages, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinner.adapter = adapter

        // load local persisted prefs to reflect current state immediately
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val localLang = prefs.getString("language", "en") ?: "en"
        val localDark = prefs.getBoolean("darkMode", false)
        switchDark.isChecked = localDark
        val idx = when (localLang) {
            "en" -> 0
            "af" -> 1
            else -> 0
        }
        spinner.setSelection(idx)

        // Load current settings from RTDB (optional) and override local if present
        settingsRef.get().addOnSuccessListener { snap ->
            val language = snap.child("language").getValue(String::class.java) ?: localLang
            val darkMode = snap.child("darkMode").getValue(Boolean::class.java) ?: localDark

            switchDark.isChecked = darkMode
            val idx2 = when (language) {
                "en" -> 0
                "af" -> 1
                else -> 0
            }
            spinner.setSelection(idx2)
        }

        btnSave.setOnClickListener {
            val selectedLang = when (spinner.selectedItem.toString()) {
                "English" -> "en"
                "Afrikaans" -> "af"
                else -> "en"
            }

            val dark = switchDark.isChecked
            // Persist locally first
            prefs.edit().putString("language", selectedLang).putBoolean("darkMode", dark).apply()

            // Apply theme immediately
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            // Apply locale immediately (recreate to re-inflate resources)
            setLocale(this, selectedLang)
            recreate()

            val appSettings = mapOf(
                "language" to selectedLang,
                "darkMode" to dark
            )
            settingsRef.setValue(appSettings)
                .addOnSuccessListener { Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show() }
                .addOnFailureListener { e -> Toast.makeText(this, e.message ?: "Save failed", Toast.LENGTH_LONG).show() }

            // finish()
        }

        btnCancel.setOnClickListener { finish() }
    }

    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
