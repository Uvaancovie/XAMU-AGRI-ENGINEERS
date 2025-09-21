package com.example.xamu_wil_project.data

data class AppUser(
    val email: String? = "",
    val firstname: String? = "",
    val lastname: String? = "",
    val qualification: String? = ""
)

data class Client(
    var companyName: String? = "",
    var companyRegNum: String? = "",
    var companyType: String? = "",
    var email: String? = "",
    var id: Long? = null
)

data class Project(
    var projectName: String? = "",
    var companyName: String? = "",
    var appUserUsername: String? = "",
    var companyEmail: String? = "",
    var id: Long? = null
)

data class BiophysicalAttributes(
    val location: String = "",
    val elevation: String = "",
    val ecoregion: String = "",
    val map: String = "",
    val rainfall: String = "",
    val evapotranspiration: String = "",
    val geology: String = "",
    val waterManagementArea: String = "",
    val soilErodibility: String = "",
    val vegetationType: String = "",
    val conservationStatus: String = "",
    val fepa: String = ""
)

data class PhaseImpacts(
    val runoffHardSurfaces: String = "",
    val runoffSepticTanks: String = "",
    val sedimentInput: String = "",
    val floodPeaks: String = "",
    val pollution: String = "",
    val weedsIAP: String = ""
)

data class Note(
    var note: String? = "",
    var location: String? = ""
)
