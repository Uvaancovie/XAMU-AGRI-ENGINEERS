Xamu Wetlands — Full Project Requirements

Purpose & Scope
A mobile app for field data collection, organization and access for wetlands work. It supports:
• Project setup (per client), biophysical & impact data capture, map/route tracking,
media capture, weather snapshot, offline save + cloud sync, and basic
theming/language.
• Android app (primary) with Firebase Realtime Database + Storage, Google Sign-In +
biometrics, Mapbox and a custom Weather API.
Primary users:
• Field Scientist (collects & syncs data in the field)
• Project Manager/Admin (creates projects, reviews data)
• Client/Stakeholder (view-only export/share—read-only)
Constraints/assumptions:
• Android 8+ (minSdk 24), intermittent connectivity, POPIA-aligned handling of
location/media.

Functional Requirements (What it must do)
A. Authentication & Onboarding
1. Google Sign-In via One Tap (MainActivity).
2. User profile check in RTDB at /AppUsers/{uid}; if absent, route to RegisterActivity to
complete details (using your external User API and/or populating RTDB).
3. Session persistence; Sign out clears in-app sensitive caches.

B. Clients & Projects
5. Select Client (SelectClientActivity): list & search clients from /ClientInfo (list view,
search box).
6. Select Project (SelectProjectActivity): list & search user-scoped projects from
/ProjectsInfo filtered by client and current user email.
7. Create Project (AddProjectActivity): add to /ProjectsInfo with {companyEmail,
companyName, appUserUsername, projectName} and return to project list.

C. Field Data Capture
Add Data to Project (AddDataToProjectActivity):
- Biophysical attributes: elevation, ecoregion, MAP, rainfall seasonality,
evapotranspiration, geology, water management area, soil erodibility, vegetation
type, conservation status, FEPA features.
- Construction/operation impacts: runoff (hard surfaces), septic runoff,
sediment input, flood peaks, pollution, weeds/IAP.
- Local draft: save to SharedPreferences (localData) on Confirm.
- Sync: write to RTDB under
/ProjectData/{company}/{project}/BiophysicalAttributes and /PhaseImpacts.
- Location stamp: include Location string passed from ProjectDetailsActivity.
- User feedback: toasts + notifications.

Notifications:
- Not Synced and Synced channels; permission request on Android 13+; tap
opens app.

D. Maps, Notes, Routes
Project Details Map (ProjectDetailsActivity):
• Mapbox satellite style, user location indicator, Center toggle, Search, Add Note,
Camera, Route tracking toggle.
• Notes: free-text stored at
/ProjectData/{company}/{project}/Notes/{timestamp} with note & location.
• Routes: record device location points while tracking; on stop, prompt for route name;
save to
/ProjectData/{company}/{project}/Routes/{routeName} with {timestamp, coordinates[]}.
• Camera: capture image + description; upload to Storage
Images/{projectName}/{timestamp}.png with metadata {description, location}.

E. Weather Snapshot
11. Weather dialog: call GET https://weather-api-zxp0.onrender.com/weather?lat&lon and
display temperature, description, humidity, pressure, icon (dialog layout).

F. Search Internet
12. In-app web search (SearchInternetActivity): SearchView + WebView to quickly look up
guidance.

G. Settings
13. Settings (SettingsActivity):
• Dark/Light toggle (AppCompatDelegate).
• Language selection (en, af initially) and apply.
• Persist to RTDB: /UserSettings/{uid}/AppSettings { language, darkMode }.

H. Viewing & Export (near-term)
14. View Entries for a project (list, filter by date/type) and Export CSV/PDF (can be deferred;
minimal requirement is being able to see what’s already uploaded).

Non-Functional Requirements (How it should behave)
Performance
• Cold start ≤ 2s on mid-range device.
• Scrolling on lists: jank < 5%; heavy work off main thread.
• Image upload concurrency capped (e.g., 2) with backoff.

Offline
• All capture (forms, notes, photos, routes) usable offline; drafts queue and sync when
online.
• Current code uses SharedPreferences for forms; add lightweight queue for
notes/routes/photos if network absent.

Reliability
• Crash-free sessions ≥ 99.5%/month.
• Sync operations idempotent; avoid duplicate writes on retries.

Security & Privacy (POPIA)
• All calls over TLS 1.2+.
• Firebase Security Rules enforce per-user/project access; Storage rules restrict reads to
authorized users; signed URLs for share, if needed.
• Location is opt-in; explain use; store coarse string (as now) or {lat,lng} if needed.
• Tokens/secrets not hard-coded; use resources/remote config where needed.

Usability & Accessibility
• From login → project selected → first data saved in ≤ 3–5 taps.
• WCAG 2.1 AA: TalkBack labels, font scaling, contrast.

Maintainability
• MVVM + repositories; DI with Hilt; coroutines/Flow; Retrofit/OkHttp; WorkManager for
background sync; Detekt/Ktlint; unit tests (domain ≥ 60%).

Compatibility
• minSdk 24, targetSdk current; Mapbox & Google Services versions pinned and
consistent.

Battery & Data
• Respect Doze; schedule bulk uploads on Wi-Fi/charging (user setting).
• Compress images (~300–600 KB target).

Use Cases (concise)
1. Sign in (Google + biometric)
2. Select client & project
3. Create new project
4. Capture field data
5. Add note at location
6. Track & save route
7. Capture & upload photo
8. Weather snapshot
9. Change settings

Data Model (Realtime DB & Storage)
See README for schema details (use RTDB paths described above).

Acceptance Criteria & Definition of Done
• Each feature includes code, UI, data writes, unit tests, and manual smoke test.

Open items / Next iterations
• Offline queue: Room + WorkManager
• RBAC/Rules: implement firebase rules to restrict by user→project mapping
• View/Export UI + CSV/PDF


