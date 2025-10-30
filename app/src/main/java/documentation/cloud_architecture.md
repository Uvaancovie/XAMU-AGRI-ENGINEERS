# Cloud Architecture & Decisions

This document details the cloud architecture for the Xamu Wetlands field app, including the services used, the rationale behind these choices, and the data flow between them.

---

## 1. Core Cloud Services

We are using a hybrid approach, leveraging the strengths of two different cloud providers:

- **Firebase (Google Cloud)**: Used for authentication, metadata storage, and application configuration.
- **Cloudinary**: Used for all image storage, transformation, and delivery.

## 2. Firebase Architecture

### Services Used

- **Firebase Authentication**: Manages user sign-up, login, and identity. It provides a secure and easy-to-use authentication system.
- **Firebase Realtime Database**: A NoSQL database used to store all non-binary data, such as project details, client information, notes, and route metadata.

### Rationale

- **Rapid Development**: Firebase allows for extremely fast setup and development, with excellent SDKs for Android.
- **Real-time Capabilities**: The Realtime Database is perfect for collaborative features, as data updates are automatically pushed to all connected clients.
- **Scalability**: Firebase is a serverless platform that automatically scales with usage.

## 3. Cloudinary Architecture

### Services Used

- **Image Upload API**: Used to upload all photos captured in the field.
- **Image Transformation API**: Provides on-the-fly image transformations, such as resizing, cropping, and optimization.
- **Image Delivery CDN**: A global Content Delivery Network (CDN) that ensures fast and reliable image delivery to users worldwide.

### Rationale

- **Specialized for Media**: Cloudinary is a market leader in image and video management, offering a rich set of features that are not available in general-purpose storage solutions.
- **Performance**: The CDN ensures that images are delivered to users with low latency, which is crucial for a good user experience.
- **Cost-Effective**: For our use case, Cloudinary's free tier is generous, and its pricing is very competitive as we scale.

## 4. Data Flow

1. A user logs in to the app using Firebase Authentication.
2. The app fetches project and client metadata from the Firebase Realtime Database.
3. When a user captures a photo, the image is uploaded directly to Cloudinary.
4. Upon successful upload, Cloudinary returns a public ID for the image.
5. The app then saves a reference to this public ID, along with any other metadata (caption, GPS location), to the Firebase Realtime Database.
6. When a user views a photo, the app fetches the public ID from Firebase and uses it to construct a Cloudinary URL to display the image.
