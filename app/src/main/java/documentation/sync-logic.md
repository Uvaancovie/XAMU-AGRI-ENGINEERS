# 🔄 Offline + Sync Logic

1. Save drafts → SharedPreferences/Room
2. Queue notes/routes/photos offline
3. WorkManager uploads on Wi-Fi + charging

| Data Type | Local Store | Remote Path |
|:--|:--|:--|
| Forms | SharedPrefs | /ProjectData |
| Notes | Queue JSON | /Notes |
| Routes | Queue list | /Routes |
| Photos | Cache file | Storage/Images |
