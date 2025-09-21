package com.example.xamu_wil_project.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.data.PhaseImpacts
import com.example.xamu_wil_project.util.Notifier
import com.example.xamu_wil_project.data.DataSeeder
import com.google.firebase.database.FirebaseDatabase
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.ProjectDataEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class AddDataToProjectActivity : AppCompatActivity() {

    private val reqNotifPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    private val gson = Gson()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_data)

        // Safely retrieve company and project. If missing, offer user to select a project or use demo values.
        var company = intent.getStringExtra("companyName")
        var project = intent.getStringExtra("projectName")
        val locationExtra = intent.getStringExtra("location") ?: ""
        if (company.isNullOrBlank() || project.isNullOrBlank()) {
            AlertDialog.Builder(this)
                .setTitle("No project selected")
                .setMessage("You must select a project before adding data.\nWould you like to select a project now or use a demo project for this demo?")
                .setPositiveButton("Select Project") { _, _ ->
                    startActivity(Intent(this, SelectProjectActivity::class.java))
                    finish()
                }
                .setNeutralButton("Use Demo") { _, _ ->
                    // Use first demo project if available
                    val demos = DataSeeder.getLocalProjects(this)
                    if (demos.isNotEmpty()) {
                        project = demos[0].projectName ?: "DemoProject"
                        company = demos[0].companyName ?: "DemoCompany"
                        Toast.makeText(this, "Using demo project: $project", Toast.LENGTH_SHORT).show()
                    } else {
                        // fallback defaults
                        company = company ?: "DemoCompany"
                        project = project ?: "DemoProject"
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> finish() }
                .setCancelable(false)
                .show()
            // Continue; callbacks will finish activity or set demo values before user proceeds
        }

        // Use non-null safe values after resolution above
        val safeCompany = company ?: "DemoCompany"
        val safeProject = project ?: "DemoProject"
        val dbBioRef = FirebaseDatabase.getInstance().getReference("ProjectData/$safeCompany/$safeProject/BiophysicalAttributes")
        val dbPhaseRef = FirebaseDatabase.getInstance().getReference("ProjectData/$safeCompany/$safeProject/PhaseImpacts")
        val prefs = getSharedPreferences("localData", Context.MODE_PRIVATE)
        val db = AppDatabase.getInstance(this)
        val projectDataDao = db.projectDataDao()

        // Save local
        findViewById<Button>(R.id.btnConfirmData).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                reqNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            val bio = BiophysicalAttributes(
                location = v(R.id.eTLocation), elevation = v(R.id.eTElevation), ecoregion = v(R.id.eTEcoregion),
                map = v(R.id.eTMAP), rainfall = v(R.id.eTRainfallSeasonality), evapotranspiration = v(R.id.eTMAPEvaporation),
                geology = v(R.id.eTGeology), waterManagementArea = v(R.id.eTWaterManagementArea), soilErodibility = v(R.id.eTSoilErodibility),
                vegetationType = v(R.id.eTVegetationType), conservationStatus = v(R.id.eTConservationStatus), fepa = v(R.id.eTFepaFeatures)
            )
            val phase = PhaseImpacts(
                runoffHardSurfaces = v(R.id.eTHardSurfaces), runoffSepticTanks = v(R.id.eTSepticTank),
                sedimentInput = v(R.id.eTSedimentInput), floodPeaks = v(R.id.eTFloodPeaks),
                pollution = v(R.id.eTPollution), weedsIAP = v(R.id.eTWeedsIAP)
            )
            prefs.edit().putString("BIO", serialize(bio)).putString("PHASE", serialize(phase)).apply()

            // Persist to Room (project_data) asynchronously
            lifecycleScope.launch {
                try {
                    val bioJson = gson.toJson(bio)
                    val phaseJson = gson.toJson(phase)
                    val entity = ProjectDataEntity(
                        companyName = safeCompany,
                        projectName = safeProject,
                        locationStamp = locationExtra.ifBlank { v(R.id.eTLocation) },
                        biophysicalJson = bioJson,
                        phaseJson = phaseJson
                    )
                    withContext(Dispatchers.IO) { projectDataDao.insert(entity) }
                } catch (_: Exception) { /* ignore local persist errors */ }
            }

            Toast.makeText(this, "Data saved locally", Toast.LENGTH_SHORT).show()
            Notifier.createChannels(this)
            val pi = PendingIntent.getActivity(this, 0, Intent(this, AddDataToProjectActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            Notifier.showNotSynced(this, pi)
        }

        // Sync to Firebase (from local)
        findViewById<Button>(R.id.btnSyncData).setOnClickListener {
            val bio = deserializeBio(prefs.getString("BIO", null))
            val phase = deserializePhase(prefs.getString("PHASE", null))
            if (bio == null || phase == null) { Toast.makeText(this, "Nothing to sync", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            // include location stamp if provided
            if (locationExtra.isNotBlank()) {
                dbBioRef.child("LocationStamp").setValue(locationExtra)
                dbPhaseRef.child("LocationStamp").setValue(locationExtra)
            }
            dbBioRef.setValue(bio)
            dbPhaseRef.setValue(phase)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data synced", Toast.LENGTH_SHORT).show()
                    Notifier.createChannels(this)
                    val pi = PendingIntent.getActivity(this, 0, Intent(this, AddDataToProjectActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
                    Notifier.showSynced(this, pi)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error syncing: ${e.message}", Toast.LENGTH_LONG).show()
                    val pi = PendingIntent.getActivity(this, 0, Intent(this, AddDataToProjectActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
                    Notifier.showNotSynced(this, pi)
                }
        }
    }

    private fun v(id: Int) = findViewById<EditText>(id).text.toString().trim()
    private fun serialize(b: Any) = gson.toJson(b)
    private fun deserializeBio(s: String?): BiophysicalAttributes? = s?.let { gson.fromJson(it, BiophysicalAttributes::class.java) }
    private fun deserializePhase(s: String?): PhaseImpacts? = s?.let { gson.fromJson(it, PhaseImpacts::class.java) }
}
