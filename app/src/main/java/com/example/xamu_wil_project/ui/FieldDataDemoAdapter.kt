        holder.soilType.text = item.soilType
        holder.weather.text = item.weather
        holder.impact.text = item.impact
    }

    override fun getItemCount() = data.size
}
package com.example.xamu_wil_project.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xamu_wil_project.R

class FieldDataDemoAdapter(private val data: List<FieldDataDemo>) : RecyclerView.Adapter<FieldDataDemoAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val projectName: TextView = view.findViewById(R.id.textProjectName)
        val location: TextView = view.findViewById(R.id.textLocation)
        val elevation: TextView = view.findViewById(R.id.textElevation)
        val ecoregion: TextView = view.findViewById(R.id.textEcoregion)
        val rainfall: TextView = view.findViewById(R.id.textRainfall)
        val soilType: TextView = view.findViewById(R.id.textSoilType)
        val weather: TextView = view.findViewById(R.id.textWeather)
        val impact: TextView = view.findViewById(R.id.textImpact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_field_data_demo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.projectName.text = item.projectName
        holder.location.text = item.location
        holder.elevation.text = item.elevation
        holder.ecoregion.text = item.ecoregion
        holder.rainfall.text = item.rainfall

