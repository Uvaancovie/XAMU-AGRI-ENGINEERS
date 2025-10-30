# Running Costs

This document outlines the estimated monthly running costs for the Xamu Wetlands field app. The costs are broken down by service and are based on the current architecture.

---

## 1. Summary

Our architecture is designed to be extremely cost-effective, especially in the initial stages. Both Firebase and Cloudinary offer generous free tiers that should be sufficient for our initial user base.

**Estimated Monthly Cost (at current scale): $0.00**

---

## 2. Firebase Costs

- **Firebase Authentication**: The first 10,000 authentications per month are free. We do not anticipate exceeding this in the short term.
- **Firebase Realtime Database**: The free tier includes 1 GB of storage and 10 GB of data transfer per month. Our metadata storage is very small, so we are unlikely to exceed these limits.

### Scaling Costs

If we exceed the free tier, the costs are as follows:

- **Authentication**: $0.01 per verification after the first 10,000.
- **Realtime Database**: $5 per GB of storage and $1 per GB of data transfer.

---

## 3. Cloudinary Costs

- **Free Tier**: Cloudinary's free tier includes 25 credits, which is equivalent to 25,000 transformations or 25 GB of storage. This is more than sufficient for our current needs.

### Scaling Costs

If we exceed the free tier, we will need to upgrade to a paid plan. The pricing is based on credits, which are used for storage, transformations, and bandwidth. The cost will depend on the number of users and the number of photos they upload.

---

## 4. Cost Optimization

- **Image Transformations**: We are using Cloudinary's on-the-fly image transformations to reduce the size of the images delivered to the app. This helps to reduce bandwidth costs.
- **Data Caching**: The app caches data offline, which reduces the number of reads from the Firebase database.
