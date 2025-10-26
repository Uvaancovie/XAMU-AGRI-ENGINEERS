package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyEmail: String,
    val companyName: String,
    val companyRegNum: String = "",
    val companyType: String = "",
    val contactPerson: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension function to convert entity to model
fun ClientEntity.toModel() = com.example.xamu_wil_project.data.Client(
    id = this.id,
    companyName = this.companyName,
    companyRegNum = this.companyRegNum,
    companyType = this.companyType,
    email = this.companyEmail
)
