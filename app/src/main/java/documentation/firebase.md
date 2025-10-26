# 🔥 Firebase Configuration

## ⚠️ CRITICAL: Authentication Required

Your app is experiencing "Error fetching token: 1 out of 2 underlying tasks failed" because:

1. **Firebase Realtime Database requires authentication** - Users must be logged in to read/write data
2. **Your app has Firebase Auth enabled** - This is correct!
3. **Solution**: Users MUST log in through the LoginScreen before accessing any features

## Current Issue and Fix

### Problem
The logs show:
```
D/PersistentConnection: pc_0 - Error fetching token: java.util.concurrent.ExecutionException: 1 out of 2 underlying tasks failed
D/RepoOperation: Aborting transactions for path: /ClientInfo/-ObAUadA_duSnb406ToO
```

This happens when trying to save data without being authenticated.

### Solution: Update Firebase Security Rules

Go to [Firebase Console](https://console.firebase.google.com/) → Your Project (xamu-wil) → Realtime Database → Rules

**For Testing/Development** (temporary - allows unauthenticated access):
```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

**For Production** (secure - requires authentication):
```json
{
  "rules": {
    "ClientInfo": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "ProjectsInfo": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "ProjectData": {
      "$companyName": {
        "$projectName": {
          ".read": "auth != null",
          ".write": "auth != null"
        }
      }
    },
    "AppUsers": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "UserSettings": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## How Authentication Works in Your App

1. **App launches** → SplashScreen checks authentication status
2. **If not logged in** → Navigate to LoginScreen
3. **User logs in** → Firebase Auth creates authenticated session
4. **Navigate to Dashboard** → User can now access all features
5. **All Firebase operations** → Use the authenticated user's token

## Testing Authentication

### Test User Login
1. Create a test user in Firebase Console → Authentication → Add User
2. Or use your LoginScreen to register/sign in
3. Once logged in, try adding a client - it should work!

### Verify Authentication State
Check logs for:
```
D/XamuWetlandsApp: Firebase initialized successfully with database URL: https://xamu-wil-default-rtdb.firebaseio.com/
D/FirebaseRepository: Attempting to add client: [client name]
D/FirebaseRepository: Client added successfully with key: [firebase key]
```

If you see "Client added successfully", authentication is working!

## Quick Fix for Immediate Testing

**Option 1: Use Open Security Rules (Not Secure - Dev Only)**
Update Firebase rules to allow all access (see above), then you can test without logging in.

**Option 2: Ensure User is Logged In (Recommended)**
1. Run the app
2. It should show LoginScreen if not authenticated
3. Log in with a valid Firebase Auth account
4. Now try adding clients - it will work!

# Firebase Configuration

1. Create project → Add Android app `com.xamu.wetlands`
2. Download `google-services.json` → `/app`
3. Enable Google Sign-In, Realtime DB, Storage
4. Rules example (POPIA-safe):

```json
{
  "rules": {
    "AppUsers": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

# Firebase Realtime Database Configuration

## Database URL
```
https://xamu-wil-default-rtdb.firebaseio.com/
```

## Project Information
- **Project ID**: xamu-wil
- **Project Number**: 722862111675
- **Storage Bucket**: xamu-wil.firebasestorage.app

## Database Structure

### ClientInfo
Stores all client information:
```json
{
  "ClientInfo": {
    "clientId": {
      "companyName": "string",
      "companyRegNum": "string",
      "companyType": "string",
      "companyEmail": "string",
      "contactPerson": "string",
      "phoneNumber": "string",
      "address": "string"
    }
  }
}
```

### ProjectsInfo
Stores all project information:
```json
{
  "ProjectsInfo": {
    "projectId": {
      "projectName": "string",
      "companyName": "string",
      "appUserUsername": "string",
      "timestamp": "number"
    }
  }
}
```

### ProjectData
Stores all field data for each project:
```json
{
  "ProjectData": {
    "companyName": {
      "projectName": {
        "Biophysical": { /* biophysical attributes */ },
        "Impacts": { /* phase impacts */ },
        "Notes": { /* array of notes */ },
        "Routes": { /* array of GPS routes */ },
        "Photos": { /* array of field photos */ },
        "WeatherSoil": { /* array of weather and soil data */ }
      }
    }
  }
}
```

## Security Rules (Development)

For **DEVELOPMENT ONLY**, use these rules to allow full access:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

⚠️ **WARNING**: These rules allow anyone to read and write to your database. Use only during development!

## Security Rules (Production)

For **PRODUCTION**, use authenticated access only:

```json
{
  "rules": {
    "ClientInfo": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "ProjectsInfo": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "ProjectData": {
      "$companyName": {
        "$projectName": {
          ".read": "auth != null",
          ".write": "auth != null"
        }
      }
    },
    "AppUsers": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "UserSettings": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## How to Update Security Rules

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **xamu-wil**
3. Click on **Realtime Database** in the left sidebar
4. Click on the **Rules** tab
5. Paste the appropriate rules (development or production)
6. Click **Publish**

## Current Configuration

The app is configured with:
- ✅ Firebase Realtime Database URL set explicitly
- ✅ Offline persistence enabled
- ✅ Debug logging enabled for troubleshooting
- ✅ Firebase App Check disabled (for development)

## Troubleshooting

### Issue: Cannot save clients
**Solution**: Make sure your Firebase Realtime Database security rules allow writes. During development, use the development rules shown above.

### Issue: "Permission denied" errors
**Solution**: 
1. Check that you're logged in (auth != null)
2. Verify security rules allow the operation
3. For testing, temporarily use the development rules

### Issue: Data not syncing
**Solution**:
1. Check internet connection
2. Verify the database URL is correct
3. Check Firebase Console for any service outages
4. Review logs using: `adb logcat | grep Firebase`

## Testing the Connection

After deploying the app, you should see this log message:
```
D/XamuWetlandsApp: Firebase initialized successfully with database URL: https://xamu-wil-default-rtdb.firebaseio.com/
```

When saving a client, you should see:
```
D/FirebaseRepository: Attempting to add client: [client name]
D/FirebaseRepository: Client added successfully with key: [firebase key]
```
