# Data Schema (ERDs & JSON)

This document provides a detailed breakdown of the data schema used in the Xamu Wetlands field app. It includes an Entity-Relationship Diagram (ERD) description for our Firebase data and JSON examples for our Cloudinary manifest.

---

## 1. Firebase Realtime Database ERD

Our Firebase database follows a NoSQL, JSON-based structure. Below is a text-based representation of the entity relationships.

```
// Top-Level Nodes

// Stores all client information
ClientInfo/
  - {clientId1}/
    - companyName: "Client A"
    - contactPerson: "John Doe"
    - ... (other client fields)

// Stores all project information, linking to clients
ProjectsInfo/
  - {projectId1}/
    - projectName: "Wetland Alpha"
    - companyName: "Client A"  // Foreign Key to ClientInfo
    - appUserUsername: "user@example.com"
    - ...

// Contains all detailed data for each project
ProjectData/
  - {companyName}/
    - {projectName}/
      - Biophysical/       // A list of historical entries
        - {entryPushId1}: {BiophysicalAttributes object}
        - {entryPushId2}: {BiophysicalAttributes object}
      - Notes/
        - {notePushId1}: {Note object}
      - Routes/
        - {routeId1}: {Route object}
      - Photos/
        - {photoId1}: {FieldPhoto object with Cloudinary public_id}

// User-specific application settings
UserSettings/
  - {firebaseUserId1}/
    - AppSettings: {UserSettings object}

// User profile information
AppUsers/
  - {firebaseUserId1}: {AppUser object}

```

### Relationships:

- **One-to-Many**: A `Client` can have multiple `Projects`.
- **One-to-Many**: A `Project` can have multiple `Notes`, `Routes`, `Photos`, and `Biophysical` data entries.

---

## 2. Cloudinary Manifest JSON Schema

For each project, a `manifest.json` file is stored in Cloudinary. This file acts as a lightweight, serverless index of all photos associated with that project.

**Path**: `raw/upload/xamu-field/{projectName}/manifest.json`

### JSON Schema Example:

```json
{
  "projectId": "Wetland Alpha",
  "items": [
    {
      "publicId": "xamu-field/Wetland Alpha/1678886400000-abc-123.jpg",
      "caption": "Photo of the riverbank.",
      "createdAt": 1678886400000
    },
    {
      "publicId": "xamu-field/Wetland Alpha/1678886460000-def-456.jpg",
      "caption": "Close-up of a specific plant species.",
      "createdAt": 1678886460000
    }
  ]
}
```

- **`projectId`**: The name of the project, which matches the folder name in Cloudinary.
- **`items`**: An array of `ProjectPhotoItem` objects.
  - **`publicId`**: The full, unique identifier for the image in Cloudinary.
  - **`caption`**: The user-provided caption for the image.
  - **`createdAt`**: A Unix timestamp (in milliseconds) of when the photo was uploaded.
