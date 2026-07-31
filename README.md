# Linkzary

> A clean, ad-free link & bookmark manager with RSS reading, an in-app article reader, and tags/collections — built with Kotlin & Jetpack Compose.

[![Build](https://github.com/ZaryabKhan/Linkzary/actions/workflows/build.yml/badge.svg)](https://github.com/ZaryabKhan/Linkzary/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Linkzary helps you save, organize, and read the web. No accounts, no ads, no tracking — your data stays on your device.

## ✨ Features

- **Save links** — quickly save any URL with automatic title, favicon, and preview-image extraction.
- **Collections** — group related links into collections.
- **Tags** — organize links with tags and multi-select actions.
- **RSS feeds** — subscribe to and read your favorite feeds in one place.
- **Article reader** — a distraction-free reader that extracts the main content from articles.
- **Link health checks** — a background worker periodically checks whether your saved links are still reachable.
- **Import / Export** — back up and restore your data via JSON or CSV.
- **Themes** — Material 3 design with light, dark, and system themes.
- **Multi-language** — English, Arabic, German, Spanish, French, Italian, and Portuguese.
- **Donations** — optional in-app donations via Google Play Billing. The app is and will remain free.
- **No ads, no analytics, no accounts.**

## 🛠️ Tech stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **MVVM** architecture with **Hilt** for dependency injection
- **Room** database
- **WorkManager** for background link-health checks
- **OkHttp** + **Jsoup** + RSS Parser for network & content extraction
- **Coil** for image loading
- **Google Play Billing** for donations

## 📋 Requirements

- Android Studio (a version supporting AGP 9.x / Gradle 9.3)
- JDK 17 or newer
- Android SDK 36 (`compileSdk = 36`, `minSdk = 26`)

## 🔨 Building

```bash
# 1. Clone the repository
git clone https://github.com/ZaryabKhan/Linkzary.git
cd Linkzary

# 2. Build a debug APK
./gradlew assembleDebug

# 3. Run unit tests
./gradlew test
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

> The project does **not** ship a machine-specific `org.gradle.java.home` in version control, so command-line builds use the JDK on your `JAVA_HOME`/`PATH`. If you want to pin a specific JDK for local CLI builds, set `org.gradle.java.home=...` in `~/.gradle/gradle.properties` (user-level, not committed).

## 🤝 Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) to learn how to set up the project and submit pull requests.

Looking for a good place to start? Check for issues labeled `good first issue`.

## 🔒 Security

Found a vulnerability? Please **do not** open a public issue. See [SECURITY.md](SECURITY.md) for how to report it privately.

## 📜 License

Linkzary is licensed under the **MIT License** — see [LICENSE](LICENSE).
