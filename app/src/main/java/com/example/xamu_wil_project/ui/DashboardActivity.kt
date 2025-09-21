package com.example.xamu_wil_project.ui

import android.content.Intent
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.widget.Button
import android.widget.TextView
import android.widget.Spinner
import android.widget.EditText
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.xamu_wil_project.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.MenuItem
import android.widget.Toast
import com.example.xamu_wil_project.data.DataSeeder
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collectLatest
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.WeatherSoilEntity
import com.example.xamu_wil_project.data.local.ClientEntity

/**
 * Simple dashboard that shows the main features and navigational buttons to other screens.
 * Designed to be a central hub immediately after sign-in.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val dashboardClients = mutableListOf<ClientEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()

        // Dashboard quick-entry views (direct lookup is preferred)
        val spinnerClients = findViewById<Spinner?>(R.id.spinnerDashboardClients)
        val etTemp = findViewById<EditText?>(R.id.etDashboardTemp)
        val etHum = findViewById<EditText?>(R.id.etDashboardHumidity)
        val etSoilMoist = findViewById<EditText?>(R.id.etDashboardSoilMoisture)
        val etSoilPH = findViewById<EditText?>(R.id.etDashboardSoilPH)
        val btnSaveQuick = findViewById<Button?>(R.id.btnSaveDashboardWeather)

        // Prepare a simple spinner adapter (only if spinner exists)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf<String>())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClients?.adapter = spinnerAdapter

        // Load clients from Room and populate spinner
        val db = AppDatabase.getInstance(this)
        val clientDao = db.clientDao()
        val weatherDao = db.weatherSoilDao()

        lifecycleScope.launch {
            clientDao.getAllFlow().collectLatest { list ->
                dashboardClients.clear()
                dashboardClients.addAll(list)
                val names = dashboardClients.map { it.companyName.ifBlank { "(unnamed)" } }
                // update spinner adapter contents on main thread
                spinnerAdapter.clear()
                spinnerAdapter.addAll(names)
                spinnerAdapter.notifyDataSetChanged()
            }
        }

        btnSaveQuick?.setOnClickListener {
            // defensive checks for optional views
            val spinner = spinnerClients
            val pos = spinner?.selectedItemPosition ?: -1
            if (pos < 0 || pos >= dashboardClients.size) {
                Toast.makeText(this, "Please select a client", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val clientId = dashboardClients[pos].id

            val temp = etTemp?.text.toString().trim().toDoubleOrNull()
            val hum = etHum?.text.toString().trim().toDoubleOrNull()
            val soilMoist = etSoilMoist?.text.toString().trim().toDoubleOrNull()
            val soilPH = etSoilPH?.text.toString().trim().toDoubleOrNull()

            val entity = WeatherSoilEntity(
                clientId = clientId,
                temperatureC = temp,
                humidityPct = hum,
                soilMoisturePct = soilMoist,
                soilPH = soilPH,
                notes = ""
            )

            lifecycleScope.launch {
                try {
                    val rowId = withContext(Dispatchers.IO) { weatherDao.insert(entity) }
                    if (rowId > 0) {
                        Toast.makeText(this@DashboardActivity, "Observation saved", Toast.LENGTH_SHORT).show()
                        // clear quick fields
                        etTemp?.text?.clear()
                        etHum?.text?.clear()
                        etSoilMoist?.text?.clear()
                        etSoilPH?.text?.clear()
                    } else {
                        Toast.makeText(this@DashboardActivity, "Could not save observation", Toast.LENGTH_LONG).show()
                    }
                } catch (ex: Exception) {
                    Toast.makeText(this@DashboardActivity, "Error saving: ${ex.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Top app bar
        val topBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topBar.setNavigationOnClickListener { finish() }

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_clients -> {
                    startActivity(Intent(this, SelectClientActivity::class.java))
                    true
                }
                R.id.nav_projects -> {
                    startActivity(Intent(this, SelectProjectActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddProjectActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<Button>(R.id.btnClients).setOnClickListener {
            startActivity(Intent(this, SelectClientActivity::class.java))
        }
        findViewById<Button>(R.id.btnProjects).setOnClickListener {
            startActivity(Intent(this, SelectProjectActivity::class.java))
        }
        findViewById<Button>(R.id.btnAddProject).setOnClickListener {
            startActivity(Intent(this, AddProjectActivity::class.java))
        }
        findViewById<Button>(R.id.btnAddData).setOnClickListener {
            // Route to SelectProjectActivity so the user picks a project before adding data
            startActivity(Intent(this, SelectProjectActivity::class.java))
        }
        findViewById<Button>(R.id.btnAddWeatherSoil).setOnClickListener {
            startActivity(Intent(this, AddWeatherSoilActivity::class.java))
        }
        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, SearchInternetActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnProjectDetails).setOnClickListener {
            startActivity(Intent(this, ProjectDetailsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnResetDemo).setOnClickListener {
            // Reset demo data and notify the user
            try {
                DataSeeder.resetSeed(this)
                Toast.makeText(this, "Demo data reset", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(this, "Could not reset demo data: ${ex.message}", Toast.LENGTH_LONG).show()
            }
        }

        val tvIntro = findViewById<TextView>(R.id.tvDashboardIntro)
        tvIntro.text = getString(R.string.dashboard_intro)
    }
}
