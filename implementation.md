here’s a clean, copy-paste **Markdown brief** you can hand to “GPT-5” to implement the Xamu Wetlands app **with Room DB**. it’s precise about package names, files to create, APIs to expose, and acceptance criteria—so it can generate working code without guessing.

---

# Xamu Wetlands — Room DB Implementation Brief

> **Package name:** `com.example.xamu_wil_project`
> *(If you must use `com.wetlands.xamuwetlands`, substitute it everywhere.)*
> **minSdk:** 24 • **targetSdk:** 36 • **Lang:** Kotlin • **UI:** XML + AppCompat
> **Arch:** MVVM + Repository + Room + Coroutines + LiveData

## 0) Goals

* Replace field data writes with a **local Room database** (source of truth).
* Implement full “Client → Projects → Field data” flow: create, validate, list, search, edit, delete, and view.
* Keep Firebase optional for later sync (don’t block Room features on Firebase).

---

## 1) Gradle & Config

### 1.1 app/build.gradle.kts

* Use these libraries (exact versions):

```kts
val room = "2.6.1"
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Room
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room") // or kapt if the project already uses it

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
```

### 1.2 Plugins

```kts
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}
```

---

## 2) Room Schema (Entities + Relations)

Create these files under `app/src/main/java/com/example/xamu_wil_project/data/model/`:

### 2.1 Client

```kotlin
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val clientId: Long = 0L,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val company: String? = null
)
```

### 2.2 Project (+ Status enum)

```kotlin
@Entity(tableName = "projects",
    indices = [Index(value = ["clientOwnerId","name"], unique = true)]
)
data class Project(
    @PrimaryKey(autoGenerate = true) val projectId: Long = 0L,
    val clientOwnerId: Long,
    val name: String,
    val description: String? = null,
    val budgetZar: Double? = null,
    val startDateUtc: Long? = null,
    val endDateUtc: Long? = null,
    val status: ProjectStatus = ProjectStatus.PLANNED
)

enum class ProjectStatus { PLANNED, ACTIVE, ON_HOLD, COMPLETED, CANCELLED }
```

### 2.3 Field Data (Biophysical + Impacts) — 1:1 with Project

```kotlin
@Entity(tableName = "field_data",
    foreignKeys = [ForeignKey(
        entity = Project::class,
        parentColumns = ["projectId"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId")]
)
data class FieldData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val projectId: Long,
    // Biophysical
    val elevation: String? = null,
    val ecoregion: String? = null,
    val map: String? = null,
    val rainfallSeasonality: String? = null,
    val evapotranspiration: String? = null,
    val geology: String? = null,
    val waterManagementArea: String? = null,
    val soilErodibility: String? = null,
    val vegetationType: String? = null,
    val conservationStatus: String? = null,
    val fepaFeatures: String? = null,
    // Impacts
    val runoffHardSurfaces: Boolean? = null,
    val runoffSepticTanks: Boolean? = null,
    val sedimentInput: Boolean? = null,
    val floodPeaks: Boolean? = null,
    val pollution: Boolean? = null,
    val weedsIAP: Boolean? = null,
    // Location stamp (string; can add lat/lng later)
    val locationString: String? = null,
    val savedAtUtc: Long = System.currentTimeMillis()
)
```

### 2.4 Notes (N:1 Project)

```kotlin
@Entity(tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = Project::class,
        parentColumns = ["projectId"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId"), Index("timestampUtc")]
)
data class Note(
    @PrimaryKey(autoGenerate = true) val noteId: Long = 0L,
    val projectId: Long,
    val text: String,
    val locationString: String? = null,
    val timestampUtc: Long = System.currentTimeMillis()
)
```

### 2.5 Routes (polyline as JSON text)

```kotlin
@Entity(tableName = "routes",
    foreignKeys = [ForeignKey(
        entity = Project::class,
        parentColumns = ["projectId"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("projectId"), Index("name")]
)
data class RouteTrack(
    @PrimaryKey(autoGenerate = true) val routeId: Long = 0L,
    val projectId: Long,
    val name: String,
    // store coordinates as JSON: [{"lat":-25.77,"lng":29.46}, ...]
    val coordinatesJson: String,
    val timestampUtc: Long = System.currentTimeMillis()
)
```

### 2.6 File: `RoomConverters.kt`

```kotlin
class RoomConverters {
    @TypeConverter fun statusToString(s: ProjectStatus?): String? = s?.name
    @TypeConverter fun stringToStatus(raw: String?): ProjectStatus? = raw?.let { ProjectStatus.valueOf(it) }
}
```

---

## 3) DAOs

Create under `data/dao/`.

### 3.1 ClientDao

```kotlin
@Dao
interface ClientDao {
    @Insert suspend fun insert(client: Client): Long
    @Update suspend fun update(client: Client)
    @Delete suspend fun delete(client: Client)

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun observeAll(): LiveData<List<Client>>

    @Query("SELECT * FROM clients WHERE clientId = :id")
    suspend fun getById(id: Long): Client?

    @Query("SELECT COUNT(*) FROM clients WHERE LOWER(name)=LOWER(:name)")
    suspend fun countByName(name: String): Int

    @Query("SELECT * FROM clients WHERE name LIKE '%'||:q||'%' ORDER BY name ASC")
    fun searchByName(q: String): LiveData<List<Client>>
}
```

### 3.2 ProjectDao

```kotlin
@Dao
interface ProjectDao {
    @Insert suspend fun insert(project: Project): Long
    @Update suspend fun update(project: Project)
    @Delete suspend fun delete(project: Project)

    @Query("SELECT * FROM projects WHERE clientOwnerId=:clientId ORDER BY startDateUtc DESC")
    fun observeForClient(clientId: Long): LiveData<List<Project>>

    @Query("SELECT COUNT(*) FROM projects WHERE clientOwnerId=:clientId AND LOWER(name)=LOWER(:name)")
    suspend fun countDuplicateName(clientId: Long, name: String): Int

    @Query("SELECT * FROM projects WHERE projectId=:id")
    suspend fun getById(id: Long): Project?
}
```

### 3.3 FieldDataDao

```kotlin
@Dao
interface FieldDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: FieldData): Long

    @Query("SELECT * FROM field_data WHERE projectId=:projectId ORDER BY savedAtUtc DESC LIMIT 1")
    fun observeLatest(projectId: Long): LiveData<FieldData?>

    @Query("SELECT * FROM field_data WHERE projectId=:projectId ORDER BY savedAtUtc DESC")
    fun observeHistory(projectId: Long): LiveData<List<FieldData>>
}
```

### 3.4 NoteDao

```kotlin
@Dao
interface NoteDao {
    @Insert suspend fun insert(note: Note): Long
    @Delete suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE projectId=:projectId ORDER BY timestampUtc DESC")
    fun observeForProject(projectId: Long): LiveData<List<Note>>
}
```

### 3.5 RouteDao

```kotlin
@Dao
interface RouteDao {
    @Insert suspend fun insert(route: RouteTrack): Long
    @Delete suspend fun delete(route: RouteTrack)

    @Query("SELECT * FROM routes WHERE projectId=:projectId ORDER BY timestampUtc DESC")
    fun observeForProject(projectId: Long): LiveData<List<RouteTrack>>
}
```

---

## 4) Database

Create `data/AppDatabase.kt`:

```kotlin
@Database(
    entities = [Client::class, Project::class, FieldData::class, Note::class, RouteTrack::class],
    version = 1, exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao
    abstract fun fieldDataDao(): FieldDataDao
    abstract fun noteDao(): NoteDao
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "wil-local.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
```

---

## 5) Repositories (business rules)

Create under `data/repo/`.

### 5.1 ClientRepository

* `createClient(name,email,phone,company)` → require non-blank name; reject duplicates.
* expose `observeAll()`, `searchByName(q)`, `getById(id)`, `update`, `delete`.

### 5.2 ProjectRepository

* `createProject(clientId, name, description, budget, startUtc, endUtc, status)` with:

    * require clientId > 0, name non-blank
    * endUtc >= startUtc if both set
    * reject duplicate name per client
* expose `observeForClient(clientId)`, `getById`, `update`, `delete`.

### 5.3 FieldDataRepository

* `saveLatest(FieldData)` via `upsert`
* `observeLatest(projectId)`, `observeHistory(projectId)`.

### 5.4 NoteRepository / RouteRepository

* simple insert/delete + live lists per project.

*(Implement exactly as thin wrappers around DAOs; use `withContext(Dispatchers.IO)` or DAO suspend functions.)*

---

## 6) ViewModels

Create under `ui/viewmodel/`.

* **ClientListViewModel**: exposes `LiveData<List<Client>>` + `search(q)`.
* **ClientFormViewModel**: `create(...)`, `update(...)`, `delete(...)`, with `_busy`, `_error`, `_done`.
* **ProjectListViewModel** (needs `clientId`): exposes projects list + search string.
* **ProjectFormViewModel**: as previously drafted (save & validate).
* **FieldDataViewModel** (needs `projectId`): load latest, save new snapshot.
* **NoteListViewModel / RouteListViewModel**: list + add/delete.

All ViewModels use `viewModelScope` and post results to LiveData. Expose immutable `LiveData`.

---

## 7) UI (XML Activities)

*(Stick to simple AppCompat + RecyclerView + basic validation.)*

**Activities to implement (exact file paths):**

* `ui/client/ClientListActivity.kt` → `res/layout/activity_client_list.xml`
* `ui/client/AddClientActivity.kt` → `res/layout/activity_add_client.xml`
* `ui/project/ProjectListActivity.kt` → `res/layout/activity_project_list.xml`
* `ui/project/AddProjectActivity.kt` → `res/layout/activity_add_project.xml`
* `ui/field/AddDataToProjectActivity.kt` → `res/layout/activity_add_data.xml` *(bind to `FieldDataViewModel`)*
* `ui/note/NotesActivity.kt` → `res/layout/activity_notes.xml`
* `ui/route/RoutesActivity.kt` → `res/layout/activity_routes.xml`

**Minimum widgets per screen:**

* Lists: `RecyclerView`, empty state `TextView`, `SearchView`, `FloatingActionButton` to add.
* Forms: `EditText`/`Spinner`/`Button`/`ProgressBar` + inline validation (setError) and toasts.

**Navigation (explicit Intents):**

* ClientList → AddClient
* ClientList → ProjectList (passes `clientId`, `clientName`)
* ProjectList → AddProject (passes `clientId`, `clientName`)
* ProjectList → AddData / Notes / Routes (passes `projectId`, `projectName`)

---

## 8) Validation Rules

* Client: `name` required; unique (case-insensitive).
* Project: `name` required; unique per client (case-insensitive), `end >= start` when both set.
* FieldData: no required fields; save whatever is provided; include timestamp and `locationString` if available.

Show validation errors as `editText.error = "Message"` and block save until fixed.

---

## 9) Sample Adapters

Generate 3 RecyclerView adapters:

* `ui/client/ClientAdapter.kt` (click → open `ProjectListActivity`)
* `ui/project/ProjectAdapter.kt` (click → open actions sheet: Data / Notes / Routes)
* `ui/common/NoteAdapter.kt`, `ui/common/RouteAdapter.kt`

Use `ListAdapter + DiffUtil`.

---

## 10) Optional: Export (later)

* Utility to export project’s FieldData/Notes/Routes to JSON in `cacheDir` and share via `ACTION_SEND`.
* Keep out of MVP if time-constrained.

---

## 11) Manifest

Add all activities and keep launcher pointing to your existing entry (e.g., `LoginActivity` or `ClientListActivity` while testing). Example:

```xml
<activity android:name=".ui.client.ClientListActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity>

<activity android:name=".ui.client.AddClientActivity"/>
<activity android:name=".ui.project.ProjectListActivity"/>
<activity android:name=".ui.project.AddProjectActivity"/>
<activity android:name=".ui.field.AddDataToProjectActivity"/>
<activity android:name=".ui.note.NotesActivity"/>
<activity android:name=".ui.route.RoutesActivity"/>
```

---

## 12) Acceptance Criteria (copy as tests/checklist)

1. **Create Client**

    * Given a non-empty client name, when Save is tapped, then a new Client row is created and `ClientList` updates via LiveData.

2. **Create Project**

    * Given a selected client and non-empty project name, when Save is tapped, then a Project row is created with unique `(clientOwnerId, name)` and listed in `ProjectList`.

3. **Project Validation**

    * If end date < start date, saving is rejected with visible error.
    * If duplicate project name for same client, saving is rejected.

4. **Add Field Data**

    * Given a project, when user enters any subset of biophysical/impact fields and taps Save, then a `FieldData` row is upserted and appears as the latest snapshot.

5. **Notes**

    * When user saves a note, it appears at top of `NotesActivity` list for that project.

6. **Routes**

    * When user saves a route (with JSON coordinates), it appears at top of `RoutesActivity` list.

7. **Search**

    * Typing in search boxes filters clients by name and projects by name in real time.

8. **Delete**

    * Deleting a client cascades: all its projects, field data, notes, and routes are removed.

---

## 13) Implementation Steps (for GPT-5 to follow)

1. Add Gradle deps & KSP plugin; sync.
2. Create `data/model/*`, `data/dao/*`, `data/AppDatabase.kt`, `data/RoomConverters.kt`.
3. Create repositories in `data/repo/*` implementing rules above.
4. Create ViewModels in `ui/viewmodel/*` with LiveData/Coroutines.
5. Build screens & adapters:

    * ClientList, AddClient
    * ProjectList, AddProject
    * AddDataToProject (form with Save)
    * Notes list + add dialog
    * Routes list + add dialog (coordinates JSON textarea for now)
6. Wire Intents (extras: ids & names).
7. Validate + toasts; progress bars during saves.
8. Run and fix any compile/layout IDs.
9. (Optional) Add Export utilities and share intent.

---

## 14) Coding Conventions

* File names match class names; package by feature (`ui/client`, `ui/project`, `ui/field`, `data/...`).
* Long work on `Dispatchers.IO`; UI on main thread.
* Keep strings in `res/values/strings.xml`.

---

## 15) Out of Scope for This Pass

* Firebase read/write, Google Sign-In, Mapbox, Weather API, background sync—**do not** block the Room flow.
* These can be integrated later via a Sync layer.

---

## 16) Deliverables

* All Kotlin & XML files compiling with Room DB implemented.
* App launches to **ClientListActivity** (or current launcher), can create client → project → field data, notes, routes locally.
* Basic search & delete working.
* No crashes on create/list/edit/delete flows.

---

> If any ambiguity remains (IDs, labels, or layout names), prefer **the simplest widget names** that match the IDs used above (e.g., `etProjectName`, `btnSave`, `recycler`, `emptyView`, `progress`).
