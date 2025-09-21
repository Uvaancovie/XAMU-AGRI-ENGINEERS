package com.example.xamu_wil_project.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.Note
import com.example.xamu_wil_project.data.WeatherApi
import com.example.xamu_wil_project.data.WeatherResponse
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.ImageEntity

@Suppress("DEPRECATION")
class ProjectDetailsActivity : AppCompatActivity() {

    private var companyName: String? = null
    private var projectName: String? = null

    private lateinit var imageUri: Uri
    private lateinit var currentPhotoPath: String

    private val storage by lazy { FirebaseStorage.getInstance() }

    // ✅ Give lazy a concrete type, and avoid naming it `database`
    private val dbRef: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_details)

        companyName = intent.getStringExtra("companyName")
        projectName = intent.getStringExtra("projectName")
        findViewById<TextView>(R.id.tvProjectHeader).text = "Project: $projectName"

        findViewById<TextView>(R.id.tvCurrentLocation).text = intent.getStringExtra("location") ?: "Location: -"

        findViewById<Button>(R.id.btnAddNote).setOnClickListener { addNote() }
        findViewById<Button>(R.id.btnCamera).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.btnWeather).setOnClickListener { fetchWeather(-25.778, 29.464) } // Middelburg approx
        findViewById<Button>(R.id.btnEditData).setOnClickListener {
            // pass current location stamp to AddDataToProjectActivity
            val loc = findViewById<TextView>(R.id.tvCurrentLocation).text.toString()
            startActivity(
                Intent(this, AddDataToProjectActivity::class.java)
                    .putExtra("companyName", companyName)
                    .putExtra("projectName", projectName)
                    .putExtra("location", loc)
            )
        }
        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, SearchInternetActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun addNote() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter Note")
            .setView(input)
            .setPositiveButton("Save") { d, _ ->
                val note = Note(input.text.toString(), findViewById<TextView>(R.id.tvCurrentLocation).text.toString())
                // write to Firebase
                dbRef.child("ProjectData/$companyName/$projectName/Notes/${System.currentTimeMillis()}")
                    .setValue(note)
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show()

                // persist to Room asynchronously
                val db = AppDatabase.getInstance(this)
                val noteDao = db.noteDao()
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val entity = com.example.xamu_wil_project.data.local.NoteEntity(
                                companyName = companyName ?: "",
                                projectName = projectName ?: "",
                                note = note.note ?: "",
                                location = note.location ?: ""
                            )
                            noteDao.insert(entity)
                        }
                    } catch (ex: Exception) {
                        // ignore local persist errors
                    }
                }

                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        imageUri = FileProvider.getUriForFile(
            this,
            "com.example.xamu_wil_project.fileprovider", // make sure this authority matches your manifest
            file
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, 102)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 102 && resultCode == RESULT_OK) {
            val bmp = BitmapFactory.decodeFile(currentPhotoPath) ?: run {
                Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show()
                return
            }
            uploadImage(bmp)
        }
    }

    private fun uploadImage(bitmap: Bitmap) {
        val path = "Images/$projectName/${System.currentTimeMillis()}.png"
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        storage.reference.child(path).putBytes(baos.toByteArray())
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show()
                // persist metadata locally
                val db = AppDatabase.getInstance(this)
                val imageDao = db.imageDao()
                val loc = findViewById<TextView>(R.id.tvCurrentLocation).text.toString()
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val entity = ImageEntity(
                                companyName = companyName ?: "",
                                projectName = projectName ?: "",
                                storagePath = taskSnapshot.storage.path,
                                description = "",
                                location = loc
                            )
                            imageDao.insert(entity)
                        }
                    } catch (ex: Exception) {
                        // ignore local persist errors
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        lifecycleScope.launch {
            val resp: Response<WeatherResponse> = WeatherApi.create().getWeather(lat, lon)
            if (resp.isSuccessful && resp.body() != null) {
                showWeather(resp.body()!!)
            } else {
                Toast.makeText(this@ProjectDetailsActivity, "Weather failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWeather(w: WeatherResponse) {
        val v = layoutInflater.inflate(R.layout.dialog_weather_info, null)
        v.findViewById<TextView>(R.id.temperatureTextView).text = "Temperature: ${w.main.temp} °C"
        v.findViewById<TextView>(R.id.descriptionTextView).text = w.weather.firstOrNull()?.description ?: "-"
        v.findViewById<TextView>(R.id.humidityTextView).text = "Humidity: ${w.main.humidity} %"
        v.findViewById<TextView>(R.id.pressureTextView).text = "Pressure: ${w.main.pressure} hPa"
        val dialog = AlertDialog.Builder(this)
            .setTitle("Weather Info")
            .setView(v)
            .setPositiveButton("OK", null)
            .create()
        dialog.setOnShowListener {
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }
}
