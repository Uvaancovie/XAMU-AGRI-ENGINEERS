# User Stories

## Purpose & Scope

- As a field scientist, I want to collect, organize, and access wetlands field data using a mobile app, so that I can efficiently manage my work in the field.
- As a project manager/admin, I want to create projects and review collected data, so that I can oversee field activities and ensure data quality.
- As a client/stakeholder, I want to view and export project data in a read-only format, so that I can stay informed about project progress and results.

## Authentication & Onboarding

- As a user, I want to sign in using Google One Tap, so that I can securely access the app.
- As a user, I want my profile to be checked and completed if missing, so that my information is up-to-date in the system.
- As a user, I want my session to persist and sensitive data to be cleared on sign out, so that my privacy is protected.

## Clients & Projects

- As a user, I want to select and search for clients, so that I can easily find the client I am working with.
- As a user, I want to select and search for projects filtered by client and my email, so that I can quickly access relevant projects.
- As a user, I want to create new projects and see them listed, so that I can manage multiple projects efficiently.

## Field Data Capture

- As a user, I want to add biophysical and impact data to a project, so that all relevant field information is captured.
- As a user, I want to save data locally and sync it to the cloud, so that my work is not lost if I am offline.
- As a user, I want location stamps included with my data, so that I can track where data was collected.
- As a user, I want feedback via toasts and notifications, so that I know the status of my data submissions.

## Maps, Notes, Routes

- As a user, I want to view project details on a map with satellite imagery and my location, so that I can visualize field activities.
- As a user, I want to add notes at specific locations, so that I can record observations contextually.
- As a user, I want to track and save routes, so that I can document my movement in the field.
- As a user, I want to capture and upload photos with descriptions and location metadata, so that visual records are stored with context.

## Weather & Soil Data

- As a user, I want to view and add scientific weather data for a selected client, so that environmental conditions are documented.
- As a user, I want to add and view soil data on the dashboard, so that soil conditions are tracked alongside other project data.

## Search & Settings

- As a user, I want to search the internet within the app, so that I can quickly look up guidance and information.
- As a user, I want to change app settings such as theme and language, so that the app is personalized to my preferences.
- As a user, I want my settings to be saved and applied, so that my experience is consistent across sessions.

## Viewing & Export

- As a user, I want to view all entries for a project and filter them by date or type, so that I can easily find specific data.
- As a user, I want to export project data to CSV or PDF, so that I can share or analyze data outside the app.

## Non-Functional Requirements

- As a user, I want the app to start quickly and perform smoothly, so that my workflow is not interrupted.
- As a user, I want all features to work offline and sync when online, so that I am not limited by connectivity.
- As a user, I want my data to be secure and private, so that I comply with POPIA and other regulations.
- As a user, I want the app to be accessible and easy to use, so that everyone can benefit from its features.
- As a developer, I want the codebase to be maintainable and well-tested, so that future updates are easy and reliable.

## Acceptance Criteria

- Each feature includes code, UI, data writes, unit tests, and manual smoke test.
- All requirements above are met and verified before release.

## Open Items / Next Iterations

- Implement offline queue for notes, routes, and photos using Room + WorkManager.
- Add role-based access control and Firebase rules for user-project mapping.
- Enhance view/export UI and add CSV/PDF export functionality.

---

> For full data model and schema details, see the README file.
