package com.example.xamu_wil_project.ui

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.WeatherSoilEntity
import com.example.xamu_wil_project.ui.viewmodel.WeatherSoilViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Screen to add weather and soil observations for a selected client.
 */
class AddWeatherSoilActivity : AppCompatActivity() {

    private lateinit var spinnerClients: Spinner
    private lateinit var etTemperature: EditText
    private lateinit var etHumidity: EditText
    private lateinit var etPressure: EditText
    private lateinit var etSoilMoisture: EditText
    private lateinit var etSoilPH: EditText
    private lateinit var etNotes: EditText

    private val clients = mutableListOf<com.example.xamu_wil_project.data.local.ClientEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_weather_soil)

        spinnerClients = findViewById(R.id.spinnerClients)
        etTemperature = findViewById(R.id.etTemperature)
        etHumidity = findViewById(R.id.etHumidity)
        etPressure = findViewById(R.id.etPressure)
        etSoilMoisture = findViewById(R.id.etSoilMoisture)
        etSoilPH = findViewById(R.id.etSoilPH)
        etNotes = findViewById(R.id.etNotes)

        val btnSave = findViewById<View>(R.id.btnSaveWeatherSoil)

        val db = AppDatabase.getInstance(this)
        val clientDao = db.clientDao()
        // use ViewModel for DB writes
        val vm = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(WeatherSoilViewModel::class.java)

        // Observe clients and populate spinner
        lifecycleScope.launch {
            clientDao.getAllFlow().collectLatest { entities ->
                clients.clear()
                clients.addAll(entities)

                val names = clients.map { it.companyName.ifBlank { "(unnamed)" } }
                val adapter = ArrayAdapter(this@AddWeatherSoilActivity, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerClients.adapter = adapter
            }
        }

        btnSave.setOnClickListener {
            saveEntry(vm)
        }
    }

    private fun saveEntry(vm: WeatherSoilViewModel) {
        val selectedPos = spinnerClients.selectedItemPosition
        if (selectedPos < 0 || selectedPos >= clients.size) {
            Toast.makeText(this, "Please select a client", Toast.LENGTH_SHORT).show()
            return
        }
        val clientId = clients[selectedPos].id

        val temp = etTemperature.text.toString().trim().toDoubleOrNull()
        val hum = etHumidity.text.toString().trim().toDoubleOrNull()
        val pres = etPressure.text.toString().trim().toDoubleOrNull()
        val soilMoist = etSoilMoisture.text.toString().trim().toDoubleOrNull()
        val soilPH = etSoilPH.text.toString().trim().toDoubleOrNull()
        val notes = etNotes.text.toString().trim()

        val entity = WeatherSoilEntity(
            clientId = clientId,
            temperatureC = temp,
            humidityPct = hum,
            pressureHPa = pres,
            soilMoisturePct = soilMoist,
            soilPH = soilPH,
            notes = notes
        )

        // Use ViewModel to insert; it switches to IO internally and returns on main thread via callback
        vm.insert(entity) { id ->
            if (id > 0) {
                Toast.makeText(this@AddWeatherSoilActivity, "Saved observation", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@AddWeatherSoilActivity, "Could not save observation", Toast.LENGTH_LONG).show()
            }
        }
    }
}
