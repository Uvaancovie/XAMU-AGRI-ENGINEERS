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
import com.example.xamu_wil_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.xamu_wil_project.data.DataSeeder
import com.example.xamu_wil_project.data.Project

class SelectProjectActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    private val allProjects = mutableListOf<Project>()
    private val visible = mutableListOf<Project>()
    private lateinit var adapter: ArrayAdapter<String>
    private val REQ_ADD_PROJECT = 2001

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

        loadProjects()

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

        btnAdd.setOnClickListener {
            // If this screen was opened for a specific company, forward it to AddProjectActivity
            val companyToPass = if (incomingCompany.isNotBlank()) incomingCompany else ""
            startActivityForResult(Intent(this, AddProjectActivity::class.java).putExtra("companyName", companyToPass), REQ_ADD_PROJECT)
        }
    }

    private fun loadProjects() {
        val myEmail = auth.currentUser?.email
        // Use single-value read and fallback to demo projects when empty or on error.
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allProjects.clear()
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (child in snapshot.children) {
                        val p = child.getValue(Project::class.java)
                        // If user is signed in, filter by their email; otherwise include all
                        if (p != null && (myEmail == null || myEmail.isBlank() || p.appUserUsername == myEmail)) {
                            allProjects.add(p)
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
        loadProjects()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ADD_PROJECT && resultCode == RESULT_OK) {
            // Newly added project; reload list
            loadProjects()
        }
    }
}
