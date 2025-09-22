package com.example.xamu_wil_project.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xamu_wil_project.R

class FieldDataDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_field_data_demo)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFieldData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FieldDataDemoAdapter(getDemoFieldData())
    }

    private fun getDemoFieldData(): List<FieldDataDemo> {
        return listOf(
            FieldDataDemo(
                projectName = "Wetland Restoration",
                location = "Site A, Lat: -33.9, Lon: 18.4",
                elevation = "120m",
                ecoregion = "Fynbos",
                rainfall = "600mm/year",
                soilType = "Sandy Loam",
                weather = "Sunny, 22°C, Humidity 55%",
                impact = "Low runoff, minimal pollution"
            ),
            FieldDataDemo(
                projectName = "Riverbank Survey",
                location = "Site B, Lat: -34.1, Lon: 18.6",
                elevation = "80m",
                ecoregion = "Grassland",
                rainfall = "800mm/year",
                soilType = "Clay",
                weather = "Cloudy, 18°C, Humidity 70%",
                impact = "Moderate sediment input"
            )
        )
    }
}

// Data class for demo
data class FieldDataDemo(
    val projectName: String,
    val location: String,
    val elevation: String,
    val ecoregion: String,
    val rainfall: String,
    val soilType: String,
    val weather: String,
    val impact: String
)

