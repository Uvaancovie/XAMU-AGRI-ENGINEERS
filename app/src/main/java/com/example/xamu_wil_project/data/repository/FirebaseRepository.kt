package com.example.xamu_wil_project.data.repository

import android.net.Uri
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
 * Implements Firebase Realtime Database + Supabase Storage for all data operations
 */
@Singleton
class FirebaseRepository @Inject constructor(
    private val storageRepository: FirebaseStorageRepository
) {

    companion object {
        // Ensure we always connect to the intended Realtime Database URL
        private const val DATABASE_URL = "https://xamu-wil-default-rtdb.firebaseio.com/"
        private const val TAG = "FirebaseRepository"
    }

    // Use the explicit DB instance for all operations (avoids ambiguity when multiple DBs are present)
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(DATABASE_URL)
    private val auth = FirebaseAuth.getInstance()

    // Client Operations
    fun getClientsFlow(): Flow<List<Client>> = callbackFlow {
        val ref = database.getReference("ClientInfo")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clients = mutableListOf<Client>()
                for (child in snapshot.children) {
                    child.getValue(Client::class.java)?.let { clients.add(it) }
                }
                trySend(clients).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
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

    suspend fun updateClient(client: Client): Result<Unit> {
        return try {
            val query = database.getReference("ClientInfo").orderByChild("companyName").equalTo(client.companyName)
            val snapshot = query.get().await()
            for (child in snapshot.children) {
                child.ref.setValue(client).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClient(client: Client): Result<Unit> {
        return try {
            val query = database.getReference("ClientInfo").orderByChild("companyName").equalTo(client.companyName)
            val snapshot = query.get().await()
            for (child in snapshot.children) {
                child.ref.removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Project Operations
    fun getProjectsFlow(userEmail: String): Flow<List<Project>> = callbackFlow {
        val ref = database.getReference("ProjectsInfo")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val projects = mutableListOf<Project>()
                for (child in snapshot.children) {
                    child.getValue(Project::class.java)?.let { projects.add(it) }
                }
                trySend(projects).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        // filter by userEmail using query
        val query = ref.orderByChild("appUserUsername").equalTo(userEmail)
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
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
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                for (child in snapshot.children) {
                    child.getValue(Note::class.java)?.let { notes.add(it) }
                }
                trySend(notes).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
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
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routes = mutableListOf<Route>()
                for (child in snapshot.children) {
                    child.getValue(Route::class.java)?.let { routes.add(it) }
                }
                trySend(routes).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Project Data Operations
    fun getProjectDataFlow(companyName: String, projectName: String): Flow<ProjectData> = callbackFlow {
        val ref = database.getReference("ProjectData/$companyName/$projectName")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                val routes = mutableListOf<Route>()
                val photos = mutableListOf<FieldPhoto>()
                val weatherData = mutableListOf<WeatherSoilData>()
                val biophysicalData = mutableListOf<BiophysicalAttributes>()

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
                snapshot.child("Biophysical").children.forEach { child ->
                    try {
                        val entry = child.getValue(BiophysicalAttributes::class.java)
                        if (entry != null) {
                            entry.id = child.key ?: ""
                            biophysicalData.add(entry)
                        }
                    } catch (e: DatabaseException) {
                        Log.w("FirebaseRepository", "Failed to parse BiophysicalAttributes, skipping entry: ${child.key}", e)
                    }
                }

                val impacts = snapshot.child("Impacts").getValue(PhaseImpacts::class.java)

                val projectData = ProjectData(
                    biophysical = biophysicalData,
                    impacts = impacts,
                    notes = notes,
                    routes = routes,
                    photos = photos,
                    weatherData = weatherData,
                    lastUpdated = System.currentTimeMillis()
                )
                trySend(projectData).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Biophysical Data Operations
    suspend fun addBiophysicalData(
        companyName: String,
        projectName: String,
        data: BiophysicalAttributes
    ): Result<Unit> {
        return try {
            database.getReference("ProjectData/$companyName/$projectName/Biophysical").push()
                .setValue(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBiophysicalData(
        companyName: String,
        projectName: String,
        data: BiophysicalAttributes
    ): Result<Unit> {
        return try {
            if (data.id.isBlank()) {
                return Result.failure(IllegalArgumentException("Biophysical data ID for update is missing."))
            }
            val ref = database.getReference("ProjectData/$companyName/$projectName/Biophysical/${data.id}")
            ref.setValue(data).await()
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
            val ref = database.getReference("ProjectData/$companyName/$projectName/WeatherSoil").push()
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
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(AppUser::class.java)
                trySend(user).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
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
            trySend(null).isSuccess
            close()
            return@callbackFlow
        }

        val ref = database.getReference("UserSettings/$uid/AppSettings")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val settings = snapshot.getValue(UserSettings::class.java)
                trySend(settings).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Photo upload: upload to Firebase Storage (via FirebaseStorageRepository) and persist metadata to Realtime DB
    suspend fun uploadPhoto(
        companyName: String,
        projectName: String,
        photoUri: Uri,
        caption: String,
        location: String
    ): Result<FieldPhoto> {
        return try {
            Log.d(TAG, "uploadPhoto: starting for $companyName / $projectName")

            // Ensure authenticated
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

            val photoId = System.currentTimeMillis().toString()

            // Upload file to Firebase Storage (returns public URL)
            val uploadResult = storageRepository.uploadPhoto(photoUri, companyName, projectName, photoId)
            if (uploadResult.isFailure) {
                return Result.failure(uploadResult.exceptionOrNull()!!)
            }

            val publicUrl = uploadResult.getOrNull() ?: return Result.failure(Exception("Upload returned empty URL"))

            // Build FieldPhoto object
            val fieldPhoto = FieldPhoto(
                photoId = photoId,
                url = publicUrl,
                thumbnailUrl = null,
                caption = caption,
                location = location,
                timestamp = System.currentTimeMillis(),
                userId = uid,
                uploadStatus = "completed"
            )

            // Persist metadata under ProjectData/{companyName}/{projectName}/Photos/{photoId}
            val ref = database.getReference("ProjectData/$companyName/$projectName/Photos/$photoId")
            ref.setValue(fieldPhoto).await()

            Log.d(TAG, "uploadPhoto: persisted metadata for $photoId")
            Result.success(fieldPhoto)
        } catch (e: Exception) {
            Log.e(TAG, "uploadPhoto failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}