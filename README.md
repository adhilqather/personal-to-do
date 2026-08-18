# Black Gold Todo

A luxury-themed Android todo app with AMOLED black background and gold accents. Built with native Android (Kotlin, Room, Navigation Component, Material Components).

## Features

- **AMOLED Black + Luxury Gold Theme** — True black (`#000000`) backgrounds with gold (`#D4A843`) accents
- **Room Database** — Offline-first persistence with Flow + coroutines
- **Exact-Time Reminders** — AlarmManager + BroadcastReceiver for precise notifications
- **Bottom Navigation** — 4 tabs (Home, Todos, Completed, Reminders) + Settings
- **FAB Quick Add** — Floating action button for rapid task creation
- **Search & Filter** — Real-time search with filter chips (All/Active/Completed, Priority/Date)
- **Export/Import JSON** — Backup/restore via Storage Access Framework
- **Splash Screen** — Branded launch with animated logo
- **Adaptive Icons** — Proper launcher icons for all densities
- **MVVM Architecture** — ViewModel + LiveData + Repository pattern

## Screenshots

| Home | Todos | Add Task | Settings |
|------|-------|----------|----------|
| ![Home](screenshots/home.png) | ![Todos](screenshots/todos.png) | ![Add](screenshots/add.png) | ![Settings](screenshots/settings.png) |

## Download

**Latest Release APK:** [app-release.apk](app/build/outputs/apk/release/app-release.apk) (5.3 MB)

Or download from [GitHub Releases](https://github.com/adhilqather/personal-to-do/releases)

## Build from Source

### Prerequisites
- JDK 17 (Temurin recommended via SDKMAN)
- Android SDK (API 34)
- Gradle 8.9 (wrapper included)

### Build Commands
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (signed)
./gradlew assembleRelease
```

**Output locations:**
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Project Structure

```
app/
├── src/main/
│   ├── java/com/blackgold/todo/
│   │   ├── data/           # Room entities, DAO, database
│   │   ├── notification/   # AlarmManager, BroadcastReceivers
│   │   ├── repository/     # Repository pattern
│   │   ├── ui/             # Fragments, adapters, ViewModels
│   │   ├── MainActivity.kt
│   │   ├── SplashActivity.kt
│   │   └── TodoApplication.kt
│   ├── res/
│   │   ├── layout/         # XML layouts
│   │   ├── navigation/     # nav_graph.xml
│   │   ├── values/         # colors, strings, themes
│   │   ├── drawable/       # Vector icons
│   │   └── mipmap/         # Adaptive launcher icons
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 1.9.20 |
| AGP | 8.7.0 |
| Gradle | 8.9 |
| Room | 2.6.1 |
| Material Components | 1.11.0 |
| Navigation | 2.7.6 |
| Coroutines | 1.7.3 |
| Gson | 2.10.1 |

## Permissions

| Permission | Purpose |
|------------|---------|
| `POST_NOTIFICATIONS` | Show reminder notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Exact-time reminder alarms |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after reboot |
| `FOREGROUND_SERVICE` | (Future) persistent reminder service |

## License

MIT License — see [LICENSE](LICENSE) for details.

---

**Built with ❤️ for Android**
