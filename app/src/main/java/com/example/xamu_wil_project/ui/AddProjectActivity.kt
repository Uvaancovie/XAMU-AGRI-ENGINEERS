package com.example.xamu_wil_project.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.DataSeeder
import com.example.xamu_wil_project.data.Project
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.toEntity
import com.example.xamu_wil_project.data.local.toModel
import com.example.xamu_wil_project.util.ProjectAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddProjectActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private val projects = mutableListOf<Project>()
    private lateinit var listView: ListView
    private lateinit var adapter: ProjectAdapter
    private lateinit var etProjectName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        // Init Firebase (Option A)
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        val company = intent.getStringExtra("companyName") ?: ""

        val tvCompany = findViewById<TextView>(R.id.tVCompanyName)
        val tvUserEmail = findViewById<TextView>(R.id.tVUserEmail)
        etProjectName = findViewById<EditText>(R.id.eTProjectName)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmAddProject)
        listView = findViewById(R.id.lVProjects)

        adapter = ProjectAdapter(this, projects)
        listView.adapter = adapter

        // Display company and user info; allow '-' to mean unspecified
        tvCompany.text = company.ifBlank { "(unspecified)" }
        tvUserEmail.text = auth.currentUser?.email ?: "(unsigned)"

        val db = AppDatabase.getInstance(this)
        val projectDao = db.projectDao()

        // Observe local DB for live updates; if a specific company was passed, show only those projects
        lifecycleScope.launch {
            if (company.isNotBlank()) {
                projectDao.getByCompanyFlow(company).collectLatest { entities ->
                    projects.clear()
                    projects.addAll(entities.map { it.toModel() })
                    adapter.notifyDataSetChanged()
                }
            } else {
                projectDao.getAllFlow().collectLatest { entities ->
                    projects.clear()
                    projects.addAll(entities.map { it.toModel() })
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // Click a project to select and return it
        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = projects[position]
            val intent = intent
            intent.putExtra("projectName", selected.projectName)
            intent.putExtra("companyName", selected.companyName)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        // Long click to edit project name (simple inline edit)
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val selected = projects[position]
            val input = EditText(this)
            input.setText(selected.projectName)
            AlertDialog.Builder(this)
                .setTitle("Edit project name")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotBlank()) {
                        lifecycleScope.launch {
                            try {
                                val updated = selected.copy(projectName = newName)
                                // update in Room on IO dispatcher
                                withContext(Dispatchers.IO) {
                                    projectDao.update(updated.toEntity())
                                }
                                Toast.makeText(this@AddProjectActivity, "Project updated", Toast.LENGTH_SHORT).show()
                            } catch (ex: Exception) {
                                try { DataSeeder.addLocalProject(this@AddProjectActivity, Project(projectName = newName, companyName = selected.companyName, appUserUsername = selected.appUserUsername, companyEmail = selected.companyEmail)) } catch (_: Exception) {}
                                Toast.makeText(this@AddProjectActivity, "Could not update locally; saved to demo list", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        btnConfirm.setOnClickListener {
            val name = etProjectName.text.toString().trim()
            var companyName = company
            var me = auth.currentUser?.email.orEmpty()
            val email = "" // optional company email

            if (name.isBlank()) {
                Toast.makeText(this, "Please enter a project name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (companyName.isBlank() || companyName == "-" || companyName == "(unspecified)") {
                val input = EditText(this)
                input.hint = "Company Name"
                AlertDialog.Builder(this)
                    .setTitle("Enter Company Name")
                    .setView(input)
                    .setPositiveButton("OK") { _, _ ->
                        val entered = input.text.toString().trim()
                        if (entered.isBlank()) {
                            Toast.makeText(this, "Company name required to create project", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                        companyName = entered
                        createProjectLocalAndRemote(projectDao, companyName, me, email, name)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return@setOnClickListener
            }

            if (me.isBlank()) me = "demo.user@xamu.org"

            createProjectLocalAndRemote(projectDao, companyName, me, email, name)
        }
    }

    private fun createProjectLocalAndRemote(projectDao: com.example.xamu_wil_project.data.local.ProjectDao, companyName: String, me: String, email: String, projectName: String) {
        val projectObj = Project(projectName = projectName, companyName = companyName, appUserUsername = me, companyEmail = email)
        val entity = projectObj.toEntity()

        lifecycleScope.launch {
            try {
                // insert on IO
                val newId = withContext(Dispatchers.IO) {
                    projectDao.insert(entity)
                }
                // Optimistically add the new project to the top of the list so the user sees it immediately
                try {
                    val added = projectObj.copy(id = newId)
                    projects.add(0, added)
                    adapter.notifyDataSetChanged()
                    // scroll the list to top to reveal the newly added item
                    listView.smoothScrollToPosition(0)
                } catch (_: Exception) {}
                // push to Firebase in background
                try {
                    val obj = mapOf(
                        "companyEmail" to email,
                        "companyName" to companyName,
                        "appUserUsername" to me,
                        "projectName" to projectName
                    )
                    FirebaseDatabase.getInstance().getReference("ProjectsInfo").push().setValue(obj)
                } catch (_: Exception) { }

                Toast.makeText(this@AddProjectActivity, "Project created", Toast.LENGTH_SHORT).show()
                // clear the project name so the user can add more and see the new entry in the list
                etProjectName.text.clear()
                // Flow collector will refresh the list from Room; optimistic update above avoids perceived delay
            } catch (ex: Exception) {
                try { DataSeeder.addLocalProject(this@AddProjectActivity, projectObj) } catch (_: Exception) {}
                Toast.makeText(this@AddProjectActivity, "Could not add to server; saved locally", Toast.LENGTH_LONG).show()
            }
        }
    }
}
