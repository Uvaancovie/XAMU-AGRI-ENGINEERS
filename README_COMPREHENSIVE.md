XAMU-WIL PROJECT — Comprehensive README

Purpose
-------
This README is written to help a developer or an LLM quickly build, run, debug, and extend the XAMU-WIL Android app (Kotlin + Jetpack Compose + Firebase). It also documents how to switch or use Firebase Realtime Database (RTDB) instead of Firestore for CRUD operations, and how to enable simple Email/Password authentication.

Table of contents
-----------------
- Project summary
- Architecture & file map
- Prerequisites
- Quick setup & build (Windows)
- Firebase configuration (Realtime Database + Auth)
- AppCheck and debug provider guidance
- Compose-only notes (no XML required)
- Common errors and fixes seen in this repository
- Data model & recommended RTDB structure
- How to implement CRUD for Field Scientists (detailed)
- Troubleshooting: auth, network, and AppCheck errors
- Developer checklist & next steps

Project summary
---------------
- Kotlin Android app using Jetpack Compose for UI.
- Dependency injection via Hilt.
- Uses Firebase SDK for Authentication and realtime persistence (current repo originally had Firestore references but can use RTDB).
- UI live under: `app/src/main/java/com/example/xamu_wil_project/ui/compose` (screens, components).
- Non-Compose legacy Activities/Adapters exist that rely on XML layouts — see notes below about migrating or removing them.

Architecture & file map (high level)
------------------------------------
- app/src/main/java/com/example/xamu_wil_project/
  - data/                 - domain models (BiophysicalAttributes, PhaseImpacts, etc.)
  - repositories/         - code that communicates with Firebase (Auth/DB) (may contain Firestore logic)
  - ui/
    - compose/            - Jetpack Compose screens and components (preferred modern UI)
    - viewmodel/          - ViewModel classes (FieldDataViewModel, etc.)
    - (legacy) Activities - Activities using XML layouts (LoginActivity, RegisterActivity, DashboardActivity, etc.)
  - App.kt                - Application class where Firebase and AppCheck are initialized
  - util/                 - helper utilities and adapters
- app/src/main/AndroidManifest.xml
- app/google-services.json (must be added by you)

Prerequisites (Windows)
-----------------------
- Android Studio (2023+ recommended) with Android SDK and Kotlin plugin
- JDK 11+
- An Android device or emulator with Google Play services (recommended for Firebase auth flows)
- Internet access
- A Firebase project and a configured Android app (you already have RTDB URL)

Quick setup & build (Windows)
-----------------------------
1. Copy your `google-services.json` into `app/`.
2. Open the project in Android Studio and let Gradle sync.
3. Add SHA-1 and SHA-256 of your debug keystore to Firebase Console (Project settings -> Add fingerprint).

From command line (Windows cmd.exe):

```bat
:: build debug APK
gradlew.bat assembleDebug

:: install on a connected device
gradlew.bat installDebug
```

Firebase configuration (Realtime Database + Auth)
------------------------------------------------
Use the RTDB URL you provided: https://xamu-wil-default-rtdb.firebaseio.com/

1. In Firebase Console -> Realtime Database -> Create database -> Choose location, start in Test mode (or locked mode and add rules).
2. Authentication -> Sign-in method -> Enable Email/Password (for register/login flows).
3. Add `google-services.json` to `app/` (downloaded from Firebase console after app registration).

Recommended dependencies (Gradle)
--------------------------------
Make sure these Firebase dependencies are present in `app/build.gradle.kts` or `build.gradle`:

- Firebase BOM and packages (example):

```kotlin
implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-database-ktx")
implementation("com.google.firebase:firebase-appcheck-ktx")
implementation("com.google.firebase:firebase-appcheck-debug") // optional for debug
```

If you prefer to pin versions, use explicit versions instead of BOM.

AppCheck and debug provider guidance
-----------------------------------
- Logs like: "No AppCheckProvider installed" or "Error getting App Check token; using placeholder token" are informational when AppCheck isn't configured — Firebase will still work using placeholder tokens if your Realtime Database rules do not *require* AppCheck.
- For development, add the debug AppCheck provider in `App.kt`:

```kotlin
// In build.gradle add: implementation("com.google.firebase:firebase-appcheck-debug:...")
// In App.kt (onCreate):
FirebaseApp.initializeApp(this)
if (BuildConfig.DEBUG) {
    val factory = com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
    com.google.firebase.appcheck.AppCheckProviderFactory.initialize(factory)
}
```

(Use the correct package and call for the AppCheck debug provider; see Firebase docs for exact import.)

Compose-only notes (no XML required)
-----------------------------------
- The modern UI uses Jetpack Compose screens under `ui/compose`. Prefer these.
- However, there are several legacy Activities and Adapters in the code that call `setContentView(R.layout.some_layout)` and `findViewById(...)`. If you want Compose-only:
  1. Remove or refactor Activities that use XML and adapt them to Compose by setting content with `setContent { MyComposeApp() }`.
  2. Or keep the Activities but add the referenced XML resources (styles, colors, layout files) to avoid resource linking errors.
- Errors like "resource style/Theme.XamuWetlands not found" mean the manifest or activities refer to styles that don't exist. Either create the style in `values/styles.xml` or change the manifest theme to a Compose theme.

Common errors found in this repo (and fixes)
-------------------------------------------
- "Plugin already on the classpath with a different version" -> Make plugin versions consistent. Check `build.gradle.kts` in project and `app` modules and `gradle.properties`.
- "Android resource linking failed: color/primaryGreen not found" -> Colors referenced by styles or layouts are missing. Create a `res/values/colors.xml` or remove references.
- "Unresolved reference 'layout' / findViewById(...)" -> Code expects XML-based views. Either add corresponding XML resources or refactor code to Compose.
- Firebase Auth reCAPTCHA / network errors during register: often caused by emulator without Play services or missing SHA fingerprints or network restrictions. Use a physical device or a Google Play emulator and add SHA keys.

RTDB Data model & recommended structure
--------------------------------------
This project stores "Client" and "Field Data". Suggested RTDB tree structure:

/clients/{clientId}
  - companyName
  - companyRegNum
  - companyEmail
  - phoneNumber
  - address
  - contactPerson

/companies/{companyId}/projects/{projectId}/biophysical/{recordId}
  - ownerUid (uid of field scientist)
  - timestamp
  - location
  - elevation
  - ecoregion
  - meanAnnualPrecipitation
  - ... other BiophysicalAttributes fields

/companies/{companyId}/projects/{projectId}/impacts/{recordId}
  - ownerUid
  - timestamp
  - runoffHardSurfaces
  - sedimentInput
  - pollution
  - ... other PhaseImpacts fields

Writing to RTDB (conceptual example in Kotlin)
---------------------------------------------
Use the Realtime Database Kotlin extensions:

```kotlin
val db = Firebase.database("https://xamu-wil-default-rtdb.firebaseio.com/")
val clientsRef = db.getReference("clients")
val newClientRef = clientsRef.push()
newClientRef.setValue(clientObject)
```

Authentication and security rules (basic)
----------------------------------------
- RTDB rules for simple per-user access (example):

```json
{
  "rules": {
    "companies": {
      "$companyId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

- For production, scope rules to require member relationships or ownerUid checks.

How to implement Field Scientist CRUD (step-by-step)
---------------------------------------------------
1. Ensure FirebaseAuth is initialized in `App.kt` (FirebaseApp.initializeApp(this)).
2. Ensure the user can sign in with Email/Password. Basic flow:
   - Register: FirebaseAuth.createUserWithEmailAndPassword(email, password)
   - Login: FirebaseAuth.signInWithEmailAndPassword(email, password)
3. After sign-in, use `FirebaseAuth.getInstance().currentUser?.uid` as `ownerUid` on records.
4. Repository patterns:
   - FieldDataRepository should expose suspend functions: `createBiophysical(companyId, projectId, data, ownerUid)`, `updateBiophysical(...)`, `deleteBiophysical(...)`, `listBiophysical(...)`.
   - Implement these with RTDB references and `setValue()` / `removeValue()`.
5. In `FieldDataViewModel`, map UI state to repository calls using viewModelScope.launch and emit `isLoading`, `successMessage`, and `errorMessage` states.
6. Compose screen `AddDataToProjectScreen` can call `viewModel.saveBiophysicalData(...)` and `saveImpactData(...)` as the current code already does.
7. Security: store `ownerUid` and add rules that only allow writes when `auth.uid == ownerUid` (or allow company members).

Handling network/auth errors seen in logs
----------------------------------------
- "No AppCheckProvider installed" — informational; add debug AppCheck provider for dev, or disable AppCheck requirements in DB rules.
- "Unable to resolve host 'firebaseappcheck.googleapis.com'" — indicates device network/DNS issue. Verify internet connectivity and DNS.
- "Initial task failed for action RecaptchaAction(action=signUpPassword)" — reCAPTCHA relies on Play Services and proper network/credentials. Use a Google Play-enabled emulator or physical device and add SHA fingerprints.
- "No user is authenticated (auth token fails)" — ensure Email/Password sign-in completed successfully and you read currentUser after successful sign-in.

Developer checklist (what I would do next)
------------------------------------------
- [ ] Add/verify `google-services.json` in `app/`.
- [ ] Enable Email/Password auth in Firebase Console.
- [ ] Ensure debug SHA added to Firebase project settings.
- [ ] Decide: keep legacy XML Activities or refactor to Compose-only. (Refactor recommended.)
- [ ] Create `FieldDataRepository` (RTDB) and add unit tests for CRUD flows (happy path + auth failure).
- [ ] Wire repository into `FieldDataViewModel` (already present) and ensure success/error feedback flows to UI.
- [ ] Add AppCheck debug provider for development builds to reduce placeholder token noise.

Appendix: Useful commands
-------------------------
- Gradle build (Windows):

```bat
gradlew.bat assembleDebug
```

- Get debug SHA (Windows PowerShell or cmd via gradle task):

```bat
gradlew.bat signingReport
```

- Run unit tests:

```bat
gradlew.bat test
```

Contact
-------
If you'd like, I can:
- Convert legacy Activities (XML) to Compose screens.
- Implement RTDB repository and Wire it to `FieldDataViewModel` and Compose screens.
- Fix the `App.kt` initialization for AppCheck debug provider.
- Add example RTDB security rules tuned to your team model.


End of README

