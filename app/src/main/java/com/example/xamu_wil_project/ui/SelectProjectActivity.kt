package com.example.xamu_wil_project.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.xamu_wil_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.xamu_wil_project.data.DataSeeder
import com.example.xamu_wil_project.data.Project
import com.example.xamu_wil_project.data.local.toModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher

class SelectProjectActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    private val allProjects = mutableListOf<Project>()
    private val visible = mutableListOf<Project>()
    private lateinit var adapter: ArrayAdapter<String>
    private val REQ_ADD_PROJECT = 2001
    private lateinit var addProjectLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_project)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance().getReference("ProjectsInfo")

        val list: ListView = findViewById(R.id.lVProjects)
        val search: EditText = findViewById(R.id.edtSearchProjects)
        val btnAdd: Button = findViewById(R.id.btnConfirmAddProject)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        list.adapter = adapter

        // Capture incoming company name (if this activity was opened after selecting a client)
        val incomingCompany = intent.getStringExtra("companyName") ?: ""

        // Prefer local Room DB for project listing (live updates). Fallback to Firebase single-read if DB empty.
        val localDb = com.example.xamu_wil_project.data.local.AppDatabase.getInstance(this)
        val projectDao = localDb.projectDao()

        // Observe Room first
        lifecycleScope.launch {
            if (incomingCompany.isNotBlank()) {
                projectDao.getByCompanyFlow(incomingCompany).collectLatest { entities ->
                    if (entities.isNotEmpty()) {
                        allProjects.clear()
                        allProjects.addAll(entities.map { it.toModel() })
                        filter("")
                    } else {
                        // fallback to firebase single-read
                        loadProjectsFromFirebase(incomingCompany)
                    }
                }
            } else {
                projectDao.getAllFlow().collectLatest { entities ->
                    if (entities.isNotEmpty()) {
                        allProjects.clear()
                        allProjects.addAll(entities.map { it.toModel() })
                        filter("")
                    } else {
                        loadProjectsFromFirebase("")
                    }
                }
            }
        }

        search.addTextChangedListener(
            onTextChanged = { text, _, _, _ -> filter(text?.toString().orEmpty()) }
        )

        list.setOnItemClickListener { _, _, position, _ ->
            val p = visible[position]
            val intent = Intent(this, ProjectDetailsActivity::class.java)
                .putExtra("companyName", p.companyName ?: "-")
                .putExtra("projectName", p.projectName ?: "-")
            startActivity(intent)
        }

        // Register Activity Result launcher for AddProjectActivity
        addProjectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Newly added project; Room Flow will emit updates. If list is currently empty, do a firebase fallback load
                if (allProjects.isEmpty()) loadProjectsFromFirebase("")
            }
        }

        btnAdd.setOnClickListener {
            // If this screen was opened for a specific company, forward it to AddProjectActivity
            val companyToPass = if (incomingCompany.isNotBlank()) incomingCompany else ""
            val intent = Intent(this, AddProjectActivity::class.java)
                .putExtra("companyName", companyToPass)
                .putExtra("forResult", true)
            addProjectLauncher.launch(intent)
        }
    }

    private fun loadProjectsFromFirebase(incomingCompany: String) {
        val myEmail = auth.currentUser?.email
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProjects.clear()
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (child in snapshot.children) {
                        val p = child.getValue(Project::class.java)
                        // If user is signed in, filter by their email; otherwise include all
                        if (p != null && (myEmail == null || myEmail.isBlank() || p.appUserUsername == myEmail)) {
                            // if incomingCompany is provided, filter by it
                            if (incomingCompany.isBlank() || p.companyName == incomingCompany) {
                                allProjects.add(p)
                            }
                        }
                    }
                    if (allProjects.isNotEmpty()) {
                        filter("") // show all for this user or all projects
                        return
                    }
                }
                // fallback to demo projects
                allProjects.addAll(DataSeeder.getLocalProjects(this@SelectProjectActivity))
                filter("")
                Toast.makeText(this@SelectProjectActivity, "Using demo projects", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                // on error, fallback to demo
                allProjects.clear()
                allProjects.addAll(DataSeeder.getLocalProjects(this@SelectProjectActivity))
                filter("")
                Toast.makeText(this@SelectProjectActivity, "Could not load projects; using demo data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(query: String) {
        val q = query.trim().lowercase()
        visible.clear()
        visible.addAll(
            allProjects.filter {
                it.projectName?.lowercase()?.contains(q) == true
            }
        )
        adapter.clear()
        adapter.addAll(visible.map { it.projectName ?: "(unnamed project)" })
        adapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        // keep firebase fallback to ensure latest remote entries are visible when needed
        // loadProjects() // removed in favor of Room + firebase fallback
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // retained for compatibility but Activity Result API handles the callback now
        super.onActivityResult(requestCode, resultCode, data)
    }
}
