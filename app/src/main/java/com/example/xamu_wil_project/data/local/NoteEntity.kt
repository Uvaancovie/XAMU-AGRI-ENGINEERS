package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var companyName: String = "",
    var projectName: String = "",
    var note: String = "",
    var location: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

fun NoteEntity.toModel() = com.example.xamu_wil_project.data.Note(note = this.note, location = this.location)
fun com.example.xamu_wil_project.data.Note.toEntity(company: String, project: String) = NoteEntity(
    companyName = company,
    projectName = project,
    note = this.note.orEmpty(),
    location = this.location.orEmpty()
)

