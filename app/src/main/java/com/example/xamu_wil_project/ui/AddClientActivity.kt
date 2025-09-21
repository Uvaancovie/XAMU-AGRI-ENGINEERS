package com.example.xamu_wil_project.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.Client
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.ClientEntity
import com.example.xamu_wil_project.data.local.toModel
import com.example.xamu_wil_project.util.ClientAdapter
import com.google.firebase.database.FirebaseDatabase
import com.example.xamu_wil_project.data.DataSeeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddClientActivity : AppCompatActivity() {
    private lateinit var etCompany: EditText
    private lateinit var etReg: EditText
    private lateinit var etType: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var listView: ListView
    private val clients = mutableListOf<Client>()
    private lateinit var adapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_client)

        etCompany = findViewById(R.id.eTCompanyName)
        etReg = findViewById(R.id.eTCompanyRegNum)
        etType = findViewById(R.id.eTCompanyType)
        etEmail = findViewById(R.id.eTCompanyEmail)
        btnSave = findViewById(R.id.btnSaveClient)
        btnCancel = findViewById(R.id.btnCancelClient)
        listView = findViewById(R.id.lVClients)

        adapter = ClientAdapter(this, clients)
        listView.adapter = adapter

        val db = AppDatabase.getInstance(this)
        val clientDao = db.clientDao()

        // Observe local DB for live updates
        lifecycleScope.launch {
            clientDao.getAllFlow().collectLatest { entities ->
                clients.clear()
                clients.addAll(entities.map { it.toModel() })
                adapter.notifyDataSetChanged()
            }
        }

        // When clicking a client, return it as the selected result
        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = clients[position]
            val intent = Intent()
            intent.putExtra("companyName", selected.companyName)
            intent.putExtra("companyRegNum", selected.companyRegNum)
            intent.putExtra("companyType", selected.companyType)
            intent.putExtra("companyEmail", selected.email)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        btnSave.setOnClickListener {
            val company = etCompany.text.toString().trim()
            val reg = etReg.text.toString().trim()
            val type = etType.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (company.isEmpty()) {
                Toast.makeText(this, "Please fill in company name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val clientModel = Client(companyName = company, companyRegNum = reg, companyType = type, email = email)
            val entity = ClientEntity(
                companyName = clientModel.companyName.orEmpty(),
                companyRegNum = clientModel.companyRegNum.orEmpty(),
                companyType = clientModel.companyType.orEmpty(),
                email = clientModel.email.orEmpty()
            )

            // Insert locally first so the list updates immediately
            lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) { clientDao.insert(entity) }
                    // push to Firebase in background; don't wait
                    try {
                        val obj = mapOf(
                            "Company_Name" to company,
                            "Company_Registration_Number" to reg,
                            "Company_Type" to type,
                            "Email_Address" to email
                        )
                        FirebaseDatabase.getInstance().getReference("ClientInfo").push().setValue(obj)
                    } catch (_: Exception) { /* ignore firebase push errors */ }

                    Toast.makeText(this@AddClientActivity, "Client added", Toast.LENGTH_SHORT).show()

                    // clear form to allow adding more; list already updated via Flow
                    etCompany.text.clear()
                    etReg.text.clear()
                    etType.text.clear()
                    etEmail.text.clear()
                } catch (_: Exception) {
                    // fallback to DataSeeder if Room fails
                    try { DataSeeder.addLocalClient(this@AddClientActivity, clientModel) } catch (_: Exception) {}
                    Toast.makeText(this@AddClientActivity, "Could not save locally; saved to demo list", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }
}
