package com.example.xamu_wil_project.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONObject

object DataSeeder {
    private const val TAG = "DataSeeder"
    private const val PREF = "demo_data"
    private const val KEY_SEEDED = "seeded"
    private const val KEY_CLIENTS = "demo_clients"
    private const val KEY_PROJECTS = "demo_projects"

    fun seedIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        try {
            seedLocal(prefs)
            // Try to push to Firebase (best-effort). If rules block it, ignore.
            try {
                val db = FirebaseDatabase.getInstance().reference
                val clients = JSONArray(prefs.getString(KEY_CLIENTS, "[]"))
                for (i in 0 until clients.length()) {
                    val obj = clients.getJSONObject(i)
                    val id = db.child("ClientInfo").push().key ?: "client_${i}"
                    db.child("ClientInfo").child(id).child("Company_Name").setValue(obj.optString("companyName"))
                    db.child("ClientInfo").child(id).child("Company_Registration_Number").setValue(obj.optString("companyRegNum"))
                    db.child("ClientInfo").child(id).child("Company_Type").setValue(obj.optString("companyType"))
                    db.child("ClientInfo").child(id).child("Email_Address").setValue(obj.optString("email"))
                }
                val projects = JSONArray(prefs.getString(KEY_PROJECTS, "[]"))
                for (i in 0 until projects.length()) {
                    val p = projects.getJSONObject(i)
                    val id = db.child("ProjectsInfo").push().key ?: "project_${i}"
                    db.child("ProjectsInfo").child(id).setValue(mapOf(
                        "projectName" to p.optString("projectName"),
                        "companyName" to p.optString("companyName"),
                        "appUserUsername" to p.optString("appUserUsername"),
                        "companyEmail" to p.optString("companyEmail")
                    ))
                }
            } catch (ex: Exception) {
                Log.w(TAG, "Could not write demo data to Firebase (best-effort): ${ex.message}")
            }
            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
            Log.i(TAG, "Demo data seeded locally")
        } catch (ex: Exception) {
            Log.e(TAG, "Failed to seed demo data", ex)
        }
    }

    private fun seedLocal(prefs: SharedPreferences) {
        val clients = JSONArray()
        clients.put(JSONObject().apply {
            put("companyName", "Acme Wetlands Pty")
            put("companyRegNum", "ACM123456")
            put("companyType", "Private")
            put("email", "contact@acmewetlands.com")
        })
        clients.put(JSONObject().apply {
            put("companyName", "Blue River Consulting")
            put("companyRegNum", "BRC654321")
            put("companyType", "Consultant")
            put("email", "info@blueriver.co.za")
        })

        val projects = JSONArray()
        projects.put(JSONObject().apply {
            put("projectName", "Middelburg Wetland Rehab")
            put("companyName", "Acme Wetlands Pty")
            put("appUserUsername", "demo.user@xamu.org")
            put("companyEmail", "contact@acmewetlands.com")
        })
        projects.put(JSONObject().apply {
            put("projectName", "Upper Catchment Study")
            put("companyName", "Blue River Consulting")
            put("appUserUsername", "demo.user@xamu.org")
            put("companyEmail", "info@blueriver.co.za")
        })

        prefs.edit().putString(KEY_CLIENTS, clients.toString()).putString(KEY_PROJECTS, projects.toString()).apply()
    }

    fun getLocalClients(context: Context): List<Client> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arrStr = prefs.getString(KEY_CLIENTS, "[]") ?: "[]"
        val arr = JSONArray(arrStr)
        val out = mutableListOf<Client>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Client(
                companyName = o.optString("companyName"),
                companyRegNum = o.optString("companyRegNum"),
                companyType = o.optString("companyType"),
                email = o.optString("email")
            ))
        }
        return out
    }

    fun getLocalProjects(context: Context): List<Project> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arrStr = prefs.getString(KEY_PROJECTS, "[]") ?: "[]"
        val arr = JSONArray(arrStr)
        val out = mutableListOf<Project>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Project(
                projectName = o.optString("projectName"),
                companyName = o.optString("companyName"),
                appUserUsername = o.optString("appUserUsername"),
                companyEmail = o.optString("companyEmail")
            ))
        }
        return out
    }

    /**
     * Reset demo data flag so seeding will run again. Useful in debug/demo flows.
     */
    fun resetSeed(context: Context) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SEEDED, false).apply()
        seedIfNeeded(context)
    }

    // Add a client to local demo storage (keeps UI consistent if Firebase write fails)
    fun addLocalClient(context: Context, client: Client) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_CLIENTS, "[]") ?: "[]")
        val obj = JSONObject()
        obj.put("companyName", client.companyName ?: "")
        obj.put("companyRegNum", client.companyRegNum ?: "")
        obj.put("companyType", client.companyType ?: "")
        obj.put("email", client.email ?: "")
        arr.put(obj)
        prefs.edit().putString(KEY_CLIENTS, arr.toString()).apply()
    }

    // Add a project to local demo storage
    fun addLocalProject(context: Context, project: Project) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val arr = JSONArray(prefs.getString(KEY_PROJECTS, "[]") ?: "[]")
        val obj = JSONObject()
        obj.put("projectName", project.projectName ?: "")
        obj.put("companyName", project.companyName ?: "")
        obj.put("appUserUsername", project.appUserUsername ?: "")
        obj.put("companyEmail", project.companyEmail ?: "")
        arr.put(obj)
        prefs.edit().putString(KEY_PROJECTS, arr.toString()).apply()
    }
}
