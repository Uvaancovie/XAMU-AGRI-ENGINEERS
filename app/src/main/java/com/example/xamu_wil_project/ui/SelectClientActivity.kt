package com.example.xamu_wil_project.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.Client
import com.example.xamu_wil_project.util.ClientAdapter
import com.google.firebase.database.*
import com.example.xamu_wil_project.data.DataSeeder
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.toModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectClientActivity : AppCompatActivity() {
    private val clients = mutableListOf<Client>()
    private lateinit var listView: ListView
    private lateinit var adapter: ClientAdapter
    private lateinit var btnAddClient: Button
    private val REQ_ADD_CLIENT = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_client)
        listView = findViewById(R.id.lVClients)
        adapter = ClientAdapter(this, clients)
        listView.adapter = adapter
        btnAddClient = findViewById(R.id.btnAddClient)
        btnAddClient.setOnClickListener {
            startActivityForResult(Intent(this, AddClientActivity::class.java), REQ_ADD_CLIENT)
        }

        findViewById<EditText>(R.id.edtSearchClients).addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) { filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Use Room DB for live updates; fallback to Firebase/DataSeeder if empty
        val db = AppDatabase.getInstance(this)
        val clientDao = db.clientDao()

        lifecycleScope.launch {
            try {
                clientDao.getAllFlow().collectLatest { entities ->
                    clients.clear()
                    clients.addAll(entities.map { it.toModel() })
                    clients.sortBy { it.companyName }
                    adapter.notifyDataSetChanged()
                }
            } catch (ex: Exception) {
                // Fallback to Firebase like before
                loadClientsFromFirebase()
            }
        }

        listView.setOnItemClickListener { _,_,pos,_ ->
            startActivity(Intent(this, SelectProjectActivity::class.java).putExtra("companyName", clients[pos].companyName))
        }
    }

    private fun loadClientsFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("ClientInfo")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clients.clear()
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (child in snapshot.children) {
                        val c = Client(
                            companyName = child.child("Company_Name").getValue(String::class.java) ?: "",
                            companyRegNum = child.child("Company_Registration_Number").getValue(String::class.java) ?: "",
                            companyType = child.child("Company_Type").getValue(String::class.java) ?: "",
                            email = child.child("Email_Address").getValue(String::class.java) ?: ""
                        )
                        clients.add(c)
                    }
                    clients.sortBy { it.companyName }
                    adapter.notifyDataSetChanged()
                } else {
                    // Fallback to demo data
                    val demo = DataSeeder.getLocalClients(this@SelectClientActivity)
                    clients.addAll(demo)
                    clients.sortBy { it.companyName }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this@SelectClientActivity, "Using demo clients", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // On error, fallback to demo data
                clients.clear()
                clients.addAll(DataSeeder.getLocalClients(this@SelectClientActivity))
                clients.sortBy { it.companyName }
                adapter.notifyDataSetChanged()
                Toast.makeText(this@SelectClientActivity, "Could not load clients; using demo data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(q: String) {
        val filtered = clients.filter { it.companyName?.contains(q, true) == true }
        listView.adapter = ClientAdapter(this, filtered)
    }
}
