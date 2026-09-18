# ⚡ TaskPeter — Developer Task Tracker & Git Sprint Studio

[![Android CI](https://img.shields.io/badge/Android-24%20to%2036-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![F-Droid Compatible](https://img.shields.io/badge/F--Droid-Ready-1976D2?style=for-the-badge&logo=fdroid&logoColor=white)](https://f-droid.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=for-the-badge)](LICENSE)

**TaskPeter** is a native, offline-first productivity powerhouse and task management studio engineered specifically for software developers. Featuring a high-contrast **Protocol Neo-Noir cyberpunk aesthetic**, real-time **GitHub issue and pull request synchronization**, an integrated **Gemini AI Developer Copilot**, an audio-synthesized **Focus Sprint Engine**, and a **strictly local, proprietary-cloud-free storage design**.

---

## 🚀 Key Features

### 1. 📋 Developer Task Management & Quick Command Palette
- **Multi-Status Workflow**: Move tasks seamlessly through `TODO`, `IN PROGRESS`, `REVIEW`, and `DONE`.
- **Command Palette (`:add`, `:prio`, `:cat`, `:repo`)**: Type terminal-style vim/CLI shortcuts directly into the quick command bar to spawn tasks with instant tagging.
- **Git Commit Attribution**: Attach commit SHAs, commit messages, and branch names to any ticket.
- **Rich Task Modals**: Edit priority levels (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), estimated time, spent time, deadlines, and tags.

### 2. 🐙 Authentic GitHub Integration & RFC 8628 Device Flow
- **Bidirectional Repo Syncing**: Pull repositories, open issues, and pull requests directly from public or private GitHub accounts.
- **RFC 8628 OAuth Device Flow**: Pair your workstation or mobile device with one-tap code verification (`https://github.com/login/device`).
- **Repositories as Categories**: Connected repositories automatically become clickable `#repo` filter chips.
- **Safe Fallbacks & Rate Limiting**: Built-in resilience for GitHub API rate limits (HTTP 403/429) and graceful offline caching.

### 3. 🤖 Peter AI Developer Copilot (Powered by Gemini)
- **Context-Aware Coding Assistant**: Chat with Peter AI using Gemini models (`gemini-2.5-flash`, `gemini-3.5-flash`, etc.).
- **1-Tap Task Extraction**: Peter AI automatically recognizes action items from chat responses and formats them into clickable task imports.
- **Quota & Token Tracking**: Monitor usage tiers (`FREE`, `PRO`, `ENTERPRISE`) with real-time token tracking and zero unauthorized billing.
- **Offline Peter Engine**: Fallback local heuristics keep you productive even without internet access.

### 4. ⏱️ Focus Sprint Timer with Binaural Synthesizer
- **Custom Sprint Types**: `CLASSIC POMODORO` (25m), `SHORT SPRINT` (15m), `DEEP WORK` (45m), or `FLOW STATE` (60m).
- **Real-Time Sound Synthesis**: Native `AudioTrack` generative synthesizer producing Alpha Binaural Beats (10 Hz), Deep Work Drones, and Pink Focus Noise.
- **Streak & Velocity Tracking**: Log sprint history directly to local Room SQLite and maintain daily productivity streaks.

### 5. 📊 Analytics, Heatmap & Calendar Timeline
- **28-Day GitHub Contribution Heatmap**: Visual activity grid inspired by Git contribution graphs.
- **Weekly Velocity Charts**: Track completed vs. pending tasks with category distribution analytics.
- **14-Day Calendar Agenda**: Horizontal interactive date picker with deadline indicators and overdue watchdog alerts.

### 6. 🔒 Privacy-First & Full Data Portability
- **100% Offline Capable**: Powered by Room SQLite Database. Works without accounts or internet.
- **Local-Only Storage**: No proprietary cloud SDKs; all data stays in the on-device Room database.
- **Data Export & Sharing**: Export entire workspaces as sanitized JSON or human-readable Markdown standup summaries with 1-tap clipboard and share intents.

---

## 🛠️ Architecture & Tech Stack

```
com.example
├── data
│   ├── auth        # FirebaseAuthManager (local identity & GitHub PAT linking)
│   ├── db          # Room Database, DAOs (Task, Sprint, GitCommit), local-only sync manager
│   ├── gemini      # GeminiChatService (REST API + Local Heuristics)
│   ├── github      # GitHubService (Retrofit + OkHttp + Moshi)
│   ├── model       # Data Entities (TaskEntity, SprintSession, Enums)
│   └── repository  # TaskPeterRepository
├── ui
│   ├── components  # CommandPalette, TaskDetail, CelebrationParticles, etc.
│   ├── screens     # TasksScreen, CalendarScreen, FocusSprint, Analytics, PeterAI, GitHubSync
│   ├── theme       # Cyberpunk/Neo-Noir M3 Colors, Typography, Shapes
│   └── MainViewModel.kt
└── util            # DateUtils, ExportHelper, NotificationHelper, SoundPlayer
```

- **Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin 2.0+ (Coroutines, StateFlow, Flow)
- **Local Storage**: Android Room Database (SQLite) with KSP
- **Networking**: Retrofit 2 + OkHttp 3 + Moshi
- **Image Loading**: Coil Compose
- **Audio Engine**: Android Native AudioTrack (PCM Real-time Wave Synthesis)
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 📦 Building From Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or IntelliJ IDEA
- Android SDK 36 (compileSdk 36, minSdk 24)
- JDK 17 or JDK 21

### Clone & Build
```bash
git clone https://github.com/pactender/taskpeter.git
cd taskpeter

# Build Debug APK
./gradlew assembleDebug

# Output APK located at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 F-Droid

TaskPeter is packaged for F-Droid. Two ways to install:

### Add the official F-Droid repository (in review)
The packaging metadata for the official F-Droid catalog lives in
[`io.github.pactender.taskpeter.yml`](io.github.pactender.taskpeter.yml) in this repo
(`RepoType: git`, `UpdateCheckMode: Tags`). Once the app is accepted into
[fdroiddata](https://gitlab.com/fdroid/fdroiddata), it installs from f-droid.org directly.

### Verify your download (recommended)

The released APK is signed with the TaskPeter release key. Verify before installing:

- APK SHA-256 (v1.0.1): `5ba0d33de20c65a9393e38818191726676771a7fad9c502585254c267f4e9c61`
- Signing certificate SHA-256: `E118D99599C2CC2EC1494BB4E5002E4DB645503169D62E85FCF669C1B840EC62`

### Build from source (always works)
F-Droid-compatible by design: builds from source with `gradle assembleRelease`,
contains no proprietary dependencies (no Firebase or Google Play SDKs), and the
optional network services (GitHub API, Gemini API) are user-configured
(declared as the `NonFreeNet` antifeature).

### Release process (maintainers)
1. Bump `versionCode` / `versionName` in `app/build.gradle.kts`.
2. Tag and push: `git tag v1.0.1 && git push origin v1.0.1`.
3. The [Release workflow](.github/workflows/release.yml) builds the signed APK
   and AAB and publishes them to GitHub Releases automatically.
4. Signing is done in CI from repository secrets (`KEYSTORE_BASE64`,
   `STORE_PASSWORD`, `KEY_PASSWORD`); the keystore is never committed.

---

## 📄 License
Licensed under the [Apache License, Version 2.0](LICENSE).
TaskPeter respects user privacy: zero telemetry, zero trackers, and 100% on-device operation by default.
