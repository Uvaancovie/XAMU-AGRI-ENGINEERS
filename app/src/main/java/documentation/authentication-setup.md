# 🔐 Authentication Setup Guide

## Current Issue: "Initial task failed for action RecaptchaAction"

This error occurs because **Email/Password authentication is not enabled in your Firebase Console**. Firebase requires explicit activation of each authentication method.

---

## ✅ How to Fix - Enable Email/Password Authentication

### Step 1: Go to Firebase Console
1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **xamu-wil**

### Step 2: Enable Email/Password Authentication
1. Click **Authentication** in the left sidebar
2. Click the **Sign-in method** tab at the top
3. Find **Email/Password** in the providers list
4. Click on it to expand
5. Toggle **Enable** to ON
6. Click **Save**

### Step 3: (Optional) Disable Email Verification
For development/testing, you may want to disable email verification:
1. In the same Email/Password settings
2. Keep **Email link (passwordless sign-in)** DISABLED
3. This allows users to register without verifying their email first

---

## 🧪 Testing After Setup

### Register a New User
1. Launch your app
2. Go to the **Register** tab
3. Fill in:
   - First Name: John
   - Last Name: Doe
   - Email: test@example.com
   - Password: test123
   - Confirm Password: test123
4. Click **Create Account**
5. ✅ You should be logged in automatically!

### Login with Existing User
1. Go to the **Login** tab
2. Enter the email and password you registered with
3. Click **Login**
4. ✅ You should be logged in!

---

## 📱 Alternative: Create Test User Manually

If you want to test login without registration:

1. Go to Firebase Console → Authentication → Users tab
2. Click **Add User**
3. Enter:
   - Email: test@example.com
   - Password: test123
4. Click **Add User**
5. Now you can login with these credentials in your app

---

## 🔍 Verify Authentication is Working

### Check Logs
After successful registration/login, you should see:
```
D/XamuWetlandsApp: Firebase initialized successfully
D/AuthViewModel: User authenticated: test@example.com
```

### Check Firebase Console
1. Go to Firebase Console → Authentication → Users
2. You should see your registered user in the list
3. The user will have a UID and email address

---

## 🚀 Next Steps After Authentication Works

Once you can successfully register and login:

### 1. Update Firebase Security Rules
Go to Firebase Console → Realtime Database → Rules:

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
    }
  }
}
```

### 2. Test Adding Clients
1. Login with your account
2. Go to Dashboard → Clients
3. Click "Add Client"
4. Fill in client details
5. Click Save
6. ✅ Client should be saved to Firebase!

### 3. Verify in Firebase Console
1. Go to Firebase Console → Realtime Database
2. You should see your data under `/ClientInfo/`

---

## ⚠️ Troubleshooting

### Error: "Network error"
**Solution**: 
- Check your internet connection
- Make sure Firebase services are not blocked by firewall
- Try using mobile data instead of WiFi

### Error: "This email is already registered"
**Solution**: 
- Use the Login tab instead of Register
- Or use a different email address
- Or delete the user from Firebase Console and try again

### Error: "Password is too weak"
**Solution**: 
- Use at least 6 characters
- Include letters and numbers for better security

### Still Getting reCAPTCHA Error?
**Solution**:
1. Make sure you've saved the changes in Firebase Console
2. Wait 1-2 minutes for changes to propagate
3. Completely close and restart your app
4. Clear app data: Settings → Apps → Xamu Wetlands → Clear Data
5. Try registering again

---

## 📊 Current Authentication Flow

```
1. App Launches
   ↓
2. SplashScreen checks if user is logged in
   ↓
3. If NOT logged in → LoginScreen
   ↓
4. User registers or logs in with email/password
   ↓
5. Firebase Auth creates/verifies user
   ↓
6. User profile saved to Firebase Realtime Database
   ↓
7. Navigate to Dashboard
   ↓
8. User can now access all features (add clients, projects, etc.)
```

---

## 🎉 Benefits of Email/Password Auth

- ✅ **Simple**: No need for Google account
- ✅ **Fast**: Quick registration with just email and password
- ✅ **Secure**: Firebase handles password encryption and security
- ✅ **Offline Support**: Users stay logged in even offline
- ✅ **Free**: No cost for email/password authentication

---

## 📝 Summary

**The reCAPTCHA error happens because Email/Password authentication is not enabled in Firebase Console.**

**To fix it:**
1. Go to Firebase Console → Authentication → Sign-in method
2. Enable Email/Password
3. Save changes
4. Wait 1-2 minutes
5. Try registering again

**After fixing, you'll be able to:**
- ✅ Register new users
- ✅ Login with email/password
- ✅ Save clients and projects to Firebase
- ✅ Access all app features

---

## 🔗 Helpful Links

- [Firebase Console](https://console.firebase.google.com/)
- [Firebase Auth Documentation](https://firebase.google.com/docs/auth)
- [Your Project: xamu-wil](https://console.firebase.google.com/project/xamu-wil)
- [Your Realtime Database](https://console.firebase.google.com/project/xamu-wil/database)

