package com.example.xamu_wil_project.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.xamu_wil_project.R
import com.example.xamu_wil_project.data.Client
import com.example.xamu_wil_project.data.Project

class ClientAdapter(context: Context, private val items: List<Client>) : ArrayAdapter<Client>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.client_row, parent, false)
        val item = items[position]
        v.findViewById<TextView>(R.id.tVclientName).text = item.companyName ?: "-"
        v.findViewById<TextView>(R.id.tVclientReg).text = "Reg: ${item.companyRegNum ?: "-"}"
        v.findViewById<TextView>(R.id.tVclientType).text = "Type: ${item.companyType ?: "-"}"
        v.findViewById<TextView>(R.id.tVclientEmail).text = "Email: ${item.email ?: "-"}"
        return v
    }
}
class ProjectAdapter(context: Context, private val items: List<Project>) : ArrayAdapter<Project>(context, 0, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView ?: LayoutInflater.from(context).inflate(R.layout.project_row, parent, false)
        val item = items[position]
        v.findViewById<TextView>(R.id.tVprojectName).text = item.projectName ?: "-"
        v.findViewById<TextView>(R.id.tVprojectCompany).text = "Company: ${item.companyName ?: "-"}"
        return v
    }
}
