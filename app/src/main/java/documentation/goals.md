
---

### 📋 `requirements.md`
```markdown
# ✅ Functional Requirements
1. **Google Sign-In** → MainActivity  
2. **User profile bootstrap** → / AppUsers/{uid}  
3. **Session persistence** → Secure sign-out  
4. **Client list + search** → / ClientInfo  
5. **Project list + create** → / ProjectsInfo  
6. **Biophysical + Impact forms** → / ProjectData/{company}/{project}  
7. **Offline save + sync** → WorkManager  
8. **Mapbox map + notes + routes + camera**  
9. **Weather snapshot** via API  
10. **Dark/Light mode + Language toggle**  

# ⚙️ Non-Functional Requirements
- Cold start ≤ 2 s  
- TLS 1.2 +, no hard-coded tokens  
- POPIA alignment for location + media  
- WCAG 2.1 AA accessibility  
- Unit tests ≥ 60 %, Detekt/Ktlint passing  
