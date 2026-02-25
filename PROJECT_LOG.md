# PROJECT LOG - Weather App
# Duration - 16 Feb to 

## 1. Project Snapshot
- Platform: Android
- Language: Kotlin
- Stack: REST API
- Key Libraries: Retrofit, Gson, Picasso, OpenWeather

## 2. Goal
> Developed a weather app to practice API integration, JSON parsing, and dynamic UI updates in Android.

---

## 3. Core Features
- RecyclerView
- API Integration

---

## 4. Issues & Fixes
- **Issue:** UI not updating due to API error (on free tier OpenWeather API).
  **Fix:** rectifying model classes as per API response fields

- **Issue:** 401: unauthorized error from API, due to which UI not updating.
  **Fix:** used Secret Gradle Plugin to correctly get the API Key

- **Issue:** switching tabs clears last selected city weather
  **Fix:** using shared preferences to save last selected or default city
---

## 5. Key Learnings
- use of OpenWeather API

## 6. Future Updates
- 7-day forecast
- Air Quality (good, moderate, poor, etc.) or AQI display (OpenWeather's Air Pollution API)

## Version History

### v1.0 - Initial Release 
- **Release Date** 25-02-2026
- **APK** Skycast_v1.0.apk
- **Changes:**
  - Initial release
  - Basic app setup
  - Core features implemented