# Domain-Driven Design (DDD) Artifacts

This document outlines the key domain-driven design artifacts for the Xamu Wetlands field app. DDD helps us create a software model that is closely aligned with the real-world business domain of wetland assessment.

---

## 1. Bounded Contexts

Our system is divided into the following bounded contexts, each with its own specific domain model:

- **Project Management Context**: Deals with clients, projects, and users. This context is responsible for managing the relationships between these core entities.
- **Field Data Context**: Encompasses all data captured in the field, including notes, routes, photos, and biophysical attributes. This is the core domain of the application.
- **Weather Context**: A supporting context responsible for fetching and providing weather data.

## 2. Ubiquitous Language

To ensure clear communication between the development team and domain experts, we will use the following ubiquitous language:

- **Client**: A company or organization that has commissioned a wetland assessment.
- **Project**: A specific wetland assessment for a client.
- **Field Scientist**: The primary user of the application, responsible for capturing data.
- **Note**: A simple text observation with a GPS location.
- **Route**: A recorded GPS track of the scientist's path through a wetland.
- **Biophysical Data**: A detailed set of scientific measurements taken at a specific point in the wetland.

## 3. Aggregates & Entities

The following are the key aggregates and entities within our domain:

- **Client Aggregate**: The `Client` is the root of this aggregate. It is identified by its `companyName`.
- **Project Aggregate**: The `Project` is the root of this aggregate. It is identified by its `projectName` and is always associated with a `Client`.
- **ProjectData Aggregate**: This is the most complex aggregate, with `Project` as its root. It contains all the field data, including `Note`, `Route`, `FieldPhoto`, and `BiophysicalAttributes`. Each of these is an entity with its own unique identity.
