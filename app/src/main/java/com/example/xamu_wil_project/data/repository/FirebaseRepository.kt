package com.example.xamu_wil_project.data.repository

import android.util.Log
import com.example.xamu_wil_project.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Repository - Primary data source for Xamu Wetlands App
 * Implements Firebase Realtime Database + Storage for all data operations
 */
@Singleton
class FirebaseRepository @Inject constructor() {

    companion object {
        // Ensure we always connect to the intended Realtime Database URL
        private const val DATABASE_URL = "https://xamu-wil-default-rtdb.firebaseio.com/"
    }

    // Use the explicit DB instance for all operations (avoids ambiguity when multiple DBs are present)
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL)
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Client Operations
    fun getClientsFlow(): Flow<List<Client>> = callbackFlow {
        val ref = database.getReference("ClientInfo")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clients = mutableListOf<Client>()
                for (child in snapshot.children) {
                    child.getValue(Client::class.java)?.let { clients.add(it) }
                }
                trySend(clients)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addClient(client: Client): Result<String> {
        return try {
            Log.d("FirebaseRepository", "Attempting to add client: ${client.companyName}")
            val ref = database.getReference("ClientInfo").push()
            ref.setValue(client).await()
            Log.d("FirebaseRepository", "Client added successfully with key: ${ref.key}")
            Result.success(ref.key ?: "")
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Failed to add client: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Project Operations
    fun getProjectsFlow(userEmail: String): Flow<List<Project>> = callbackFlow {
        val ref = database.getReference("ProjectsInfo")
        val listener = ref.orderByChild("appUserUsername").equalTo(userEmail)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val projects = mutableListOf<Project>()
                    for (child in snapshot.children) {
                        child.getValue(Project::class.java)?.let { projects.add(it) }
                    }
                    trySend(projects)
                }
                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addProject(project: Project): Result<String> {
        return try {
            val ref = database.getReference("ProjectsInfo").push()
            ref.setValue(project).await()
            Result.success(ref.key ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Note Operations
    suspend fun addNote(companyName: String, projectName: String, note: Note): Result<String> {
        return try {
            val ref = database.getReference("ProjectData/$companyName/$projectName/Notes").push()
            ref.setValue(note).await()
            Result.success(ref.key ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getNotesFlow(companyName: String, projectName: String): Flow<List<Note>> = callbackFlow {
        val ref = database.getReference("ProjectData/$companyName/$projectName/Notes")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                for (child in snapshot.children) {
                    child.getValue(Note::class.java)?.let { notes.add(it) }
                }
                trySend(notes)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // Route Operations
    suspend fun saveRoute(companyName: String, projectName: String, route: Route): Result<String> {
        return try {
            val ref = database.getReference("ProjectData/$companyName/$projectName/Routes/${route.routeId}")
            ref.setValue(route).await()
            Result.success(route.routeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRoutesFlow(companyName: String, projectName: String): Flow<List<Route>> = callbackFlow {
        val ref = database.getReference("ProjectData/$companyName/$projectName/Routes")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<Route>()
                for (child in snapshot.children) {
                    child.getValue(Route::class.java)?.let { routes.add(it) }
                }
                trySend(routes)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // Project Data Operations
    fun getProjectDataFlow(companyName: String, projectName: String): Flow<ProjectData> = callbackFlow {
        val ref = database.getReference("ProjectData/$companyName/$projectName")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                val routes = mutableListOf<Route>()
                val photos = mutableListOf<FieldPhoto>()
                val weatherData = mutableListOf<WeatherSoilData>()

                snapshot.child("Notes").children.forEach { child ->
                    child.getValue(Note::class.java)?.let { notes.add(it) }
                }
                snapshot.child("Routes").children.forEach { child ->
                    child.getValue(Route::class.java)?.let { routes.add(it) }
                }
                snapshot.child("Photos").children.forEach { child ->
                    child.getValue(FieldPhoto::class.java)?.let { photos.add(it) }
                }
                snapshot.child("WeatherSoil").children.forEach { child ->
                    child.getValue(WeatherSoilData::class.java)?.let { weatherData.add(it) }
                }

                val biophysical = snapshot.child("Biophysical").getValue(BiophysicalAttributes::class.java)
                val impacts = snapshot.child("Impacts").getValue(PhaseImpacts::class.java)

                val projectData = ProjectData(
                    biophysical = biophysical,
                    impacts = impacts,
                    notes = notes,
                    routes = routes,
                    photos = photos,
                    weatherData = weatherData,
                    lastUpdated = System.currentTimeMillis()
                )
                trySend(projectData)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // Biophysical Data Operations
    suspend fun saveBiophysicalData(
        companyName: String,
        projectName: String,
        data: BiophysicalAttributes
    ): Result<Unit> {
        return try {
            database.getReference("ProjectData/$companyName/$projectName/Biophysical")
                .setValue(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Impact Data Operations
    suspend fun saveImpactData(
        companyName: String,
        projectName: String,
        data: PhaseImpacts
    ): Result<Unit> {
        return try {
            database.getReference("ProjectData/$companyName/$projectName/Impacts")
                .setValue(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Weather & Soil Operations
    suspend fun addWeatherSoilData(
        companyName: String,
        projectName: String,
        data: WeatherSoilData
    ): Result<String> {
        return try {
            val ref = database.getReference("ProjectData/$companyName/$projectName/WeatherSoil")
                .push()
            ref.setValue(data).await()
            Result.success(ref.key ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User Profile Operations
    suspend fun createOrUpdateUser(uid: String, user: AppUser): Result<Unit> {
        return try {
            database.getReference("AppUsers/$uid").setValue(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserProfile(uid: String): Flow<AppUser?> = callbackFlow {
        val ref = database.getReference("AppUsers/$uid")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(AppUser::class.java)
                trySend(user)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // User Settings Operations
    suspend fun saveUserSettings(settings: UserSettings): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
            database.getReference("UserSettings/$uid/AppSettings").setValue(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserSettingsFlow(): Flow<UserSettings?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val ref = database.getReference("UserSettings/$uid/AppSettings")
        val listener = ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(UserSettings::class.java)
                trySend(settings)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    // Photo Upload Operations
    suspend fun uploadPhoto(
        companyName: String,
        projectName: String,
        photoUri: android.net.Uri,
        caption: String,
        location: String
    ): Result<FieldPhoto> {
        return try {
            val photoId = System.currentTimeMillis().toString()
            val storageRef = storage.reference
                .child("projects/$companyName/$projectName/photos/$photoId.jpg")

            val uploadTask = storageRef.putFile(photoUri).await()
            val downloadUrl = storageRef.downloadUrl.await()

            val photo = FieldPhoto(
                photoId = photoId,
                url = downloadUrl.toString(),
                caption = caption,
                location = location,
                timestamp = System.currentTimeMillis(),
                userId = auth.currentUser?.uid ?: "",
                uploadStatus = "completed"
            )

            database.getReference("ProjectData/$companyName/$projectName/Photos/$photoId")
                .setValue(photo).await()

            Result.success(photo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
