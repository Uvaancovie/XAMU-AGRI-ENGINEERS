
| Layer | Components | Purpose |
|:--|:--|:--|
| UI | Activities + Fragments | Interaction & navigation |
| ViewModel | AuthVM / ProjectVM | State + validation |
| Repository | AuthRepo / ProjectRepo | Firebase + API I/O |
| Data | RTDB / Storage / Weather API | External data |

## Tools
- **DI:** Hilt
- **Async:** Coroutines + Flow
- **Offline:** WorkManager + SharedPreferences
- **Networking:** Retrofit + OkHttp
