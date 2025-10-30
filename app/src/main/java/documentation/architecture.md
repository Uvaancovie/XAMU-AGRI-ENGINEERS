# System Architecture (C4 Model)

This document outlines the architecture of the Xamu Wetlands field app using the C4 model. This approach helps to visualize the system at different levels of detail, making it easier to understand for both technical and non-technical stakeholders.

---

## Level 1: System Context

The system context diagram shows how the Xamu Wetlands app fits into the wider world.

```
+---------------------------+
|       Field Scientist     |
+---------------------------+
         |
         v
+---------------------------+
| Xamu Wetlands Mobile App  |
+---------------------------+
         |
         v
+---------------------------+
|    Backend Services       |
| (Firebase & Cloudinary)   |
+---------------------------+
```

- **Field Scientist**: The primary user of the application.
- **Xamu Wetlands Mobile App**: The Android application that the field scientist uses to capture data.
- **Backend Services**: The cloud-based services that store and manage the application's data.

---

## Level 2: Containers

The container diagram shows the high-level technical containers that make up the system.

```
+----------------------------------------------------+
| System: Xamu Wetlands App                           |
|                                                    |
| +-----------------------+   +--------------------+ |
| |      Android App      |   |  Firebase Services | |
| |       (Kotlin)        |-->| (Realtime DB, Auth)| |
| +-----------------------+   +--------------------+ |
|           |                                        |
|           v                                        |
| +-----------------------+                          |
| |   Cloudinary Service  |                          |
| |    (Image Storage)    |                          |
| +-----------------------+                          |
|                                                    |
+----------------------------------------------------+
```

- **Android App**: The mobile application itself, built with Kotlin and Jetpack Compose.
- **Firebase Services**: Provides authentication, a realtime database for metadata, and other supporting services.
- **Cloudinary Service**: Used for storing and delivering all image assets.

---

## Level 3: Components

The component diagram breaks down the Android app into its major components.

```
+----------------------------------------------------------------------+
| Container: Android App                                               |
|                                                                      |
| +------------------+   +------------------+   +---------------------+ |
| |   UI (Compose)   |-->|   ViewModels     |-->|     Repositories    | |
| +------------------+   +------------------+   +---------------------+ |
|                            |                               |         |
|                            v                               v         |
| +------------------+   +------------------+   +---------------------+ |
| |  Domain Models   |   |   Hilt (DI)      |   |   Backend Services  | |
| +------------------+   +------------------+   | (Firebase, Cloudinary)| |
|                                               +---------------------+ |
+----------------------------------------------------------------------+
```

- **UI (Compose)**: The user interface, built with Jetpack Compose.
- **ViewModels**: Manages the state for the UI and handles user interactions.
- **Repositories**: Acts as a single source of truth for all data, abstracting the data sources from the rest of the app.
- **Domain Models**: The data classes that represent the core concepts of the application.
- **Hilt (DI)**: Manages dependency injection throughout the app.
- **Backend Services**: The concrete implementations for interacting with Firebase and Cloudinary.
