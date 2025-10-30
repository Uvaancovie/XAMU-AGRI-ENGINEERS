package com.example.xamu_wil_project

import android.app.Application
import android.util.Log
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import com.example.xamu_wil_project.cloudinary.CloudinaryHelper

/**
 * Application class for Xamu Wetlands
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class XamuWetlandsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Get Firebase Database instance and configure it
            val database = FirebaseDatabase.getInstance("https://xamu-wil-default-rtdb.firebaseio.com/")

            // Enable Firebase Database offline persistence
            database.setPersistenceEnabled(true)

            // Enable logging in debug mode for troubleshooting
            FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG)

            Log.d("XamuWetlandsApp", "Firebase initialized successfully with database URL: https://xamu-wil-default-rtdb.firebaseio.com/")
        } catch (e: Exception) {
            Log.e("XamuWetlandsApp", "Error initializing Firebase", e)
        }

        // Initialize Cloudinary
        val config = hashMapOf(
            "cloud_name" to "dir468aeq",
            "secure" to true
        )
        MediaManager.init(this, config)

        // Set the upload preset used for unsigned uploads (change to match your Cloudinary preset)
        try {
            val preset = "XAMU" // user's preset name
            CloudinaryHelper.setUploadPreset(preset)
            Log.d("XamuWetlandsApp", "Cloudinary initialized with cloud_name=dir468aeq and preset=$preset")
        } catch (e: Exception) {
            Log.e("XamuWetlandsApp", "Error initializing Cloudinary preset", e)
        }
    }
}
