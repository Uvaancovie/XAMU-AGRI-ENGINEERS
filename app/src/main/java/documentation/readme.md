# 🌿 Xamu Wetlands

Android mobile application for **field data collection and synchronization** in wetland research and restoration projects.

---

## 🧱 Stack
- Kotlin + MVVM + Hilt + Coroutines
- Firebase RTDB + Storage
- Google Sign-In + Biometrics
- Mapbox SDK (Satellite + Tracking)
- Retrofit + Custom Weather API
- SharedPreferences / Room + WorkManager

---

## 🧩 Core Modules
| Module | Key Activities |
|:--|:--|
| Auth & Onboarding | Google Sign-In / RegisterActivity / Session persist |
| Clients & Projects | Client + Project lists / Create Project |
| Field Data | Biophysical + Impact forms / Sync RTDB |
| Maps & Routes | Mapbox map / Notes / Camera / Routes |
| Weather | Weather snapshot via API |
| Settings | Language + Theme + Persistence |

---

## 🚀 Quick Start
```bash
git clone https://github.com/way2flydigital/xamu-wetlands.git
cd xamu-wetlands
# Open in Android Studio Giraffe +
