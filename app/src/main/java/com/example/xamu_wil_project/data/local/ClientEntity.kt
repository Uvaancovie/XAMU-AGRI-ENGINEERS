package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var companyName: String = "",
    var companyRegNum: String = "",
    var companyType: String = "",
    var email: String = ""
)
// helper mapping
fun ClientEntity.toModel() = com.example.xamu_wil_project.data.Client(
    companyName = this.companyName,
    companyRegNum = this.companyRegNum,
    companyType = this.companyType,
    email = this.email,
    id = this.id
)
fun com.example.xamu_wil_project.data.Client.toEntity() = ClientEntity(
    id = this.id ?: 0,
    companyName = this.companyName.orEmpty(),
    companyRegNum = this.companyRegNum.orEmpty(),
    companyType = this.companyType.orEmpty(),
    email = this.email.orEmpty()
)
