# DevOps & CI/CD Pipeline

This document outlines the DevOps practices and the Continuous Integration/Continuous Deployment (CI/CD) pipeline for the Xamu Wetlands field app. Our goal is to automate as much of the build, test, and release process as possible to ensure a high-quality and reliable product.

---

## 1. CI/CD Platform

We will use **GitHub Actions** as our CI/CD platform. It is tightly integrated with our source code repository and provides a flexible and powerful way to automate our workflows.

## 2. Pipeline Overview

Our CI/CD pipeline will be triggered on every push to the `main` branch and on every pull request.

### Pipeline Stages

1. **Build**: The first stage is to build the Android application. This ensures that the code compiles and that all dependencies are correctly resolved.
2. **Test**: The second stage is to run all unit and integration tests. This verifies that the code is working as expected and that there are no regressions.
3. **Static Analysis**: We will use a static analysis tool like `ktlint` or Android's built-in lint checks to ensure that the code adheres to our style guidelines and that there are no potential bugs.
4. **Deploy (Manual)**: The final stage is to deploy the application to the Google Play Store. This will be a manual step, triggered by a tag on the `main` branch.

### Sample GitHub Actions Workflow

```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:

    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3
    - name: set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build with Gradle
      run: ./gradlew build

    - name: Run unit tests
      run: ./gradlew test
```
