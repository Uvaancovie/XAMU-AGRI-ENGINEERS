package com.example.xamu_wil_project

import android.app.Application
import android.os.StrictMode
import android.util.Log
import android.content.pm.ApplicationInfo
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.FirebaseApp
import com.example.xamu_wil_project.data.DataSeeder
import androidx.appcompat.app.AppCompatDelegate


class App : Application() {
    private val TAG = "App"
    override fun onCreate() {
        super.onCreate()
        // Use the official FirebaseApp initializer. Firebase is usually auto-initialized
        // by the google-services plugin, but calling initializeApp is safe and correct.
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized")
        } catch (ex: Exception) {
            // Don't crash the app during initialization; report and continue. This avoids
            // a hard freeze at startup when Firebase fails to initialize due to config issues.
            Log.e(TAG, "Firebase initialization failed", ex)
        }
        // Apply saved theme preference (dark mode) from local prefs before activities start
        try {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val dark = prefs.getBoolean("darkMode", false)
            AppCompatDelegate.setDefaultNightMode(if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            Log.d(TAG, "Applied saved darkMode=$dark at startup")
        } catch (ex: Exception) {
            Log.w(TAG, "Could not apply saved theme: ${ex.message}")
        }
        // Install debug App Check provider in debuggable builds to avoid "No AppCheckProvider" warnings.
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            try {
                val appCheck = FirebaseAppCheck.getInstance()
                appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
                Log.d(TAG, "AppCheck debug provider installed")
                // NOTE: avoid requesting a token immediately to prevent repeated attempts / rate limits.
                // The SDK will fetch tokens on demand; developers can obtain a debug token by
                // enabling the debug provider and inspecting logcat for provider logs if needed.
                // Seed demo data in debug so the app has professional-looking demo content.
                try { DataSeeder.seedIfNeeded(this) } catch (ex: Exception) { Log.w(TAG, "Seeder failed: ${ex.message}") }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to install AppCheck debug provider", ex)
            }
        }
        // Enable StrictMode in debug builds to surface accidental disk/network on main thread
        if (isDebuggable) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
