# Contributing to Linkzary

First off, thanks for taking the time to contribute! 🎉

The following is a set of guidelines for contributing to Linkzary. These are mostly guidelines, not rules — use your best judgment.

## How can I contribute?

### Reporting bugs

Open a [Bug Report issue](https://github.com/ZaryabKhan/Linkzary/issues/new?template=bug_report.yml) and fill in the template. The more detail, the better.

> **Security issues:** do **not** open a public issue. See [SECURITY.md](SECURITY.md).

### Suggesting enhancements

Open a [Feature Request issue](https://github.com/ZaryabKhan/Linkzary/issues/new?template=feature_request.yml).

### Pull requests

1. **Fork** the repository and create your branch from `master`.
2. **Set up** the project (see [Development setup](#development-setup) below).
3. **Make your changes**, keeping code style consistent with the rest of the codebase.
4. **Build and test** locally — make sure `./gradlew assembleDebug` and `./gradlew test` pass.
5. **Open a Pull Request** against `master` and fill in the PR template.
6. A maintainer will review your PR. You may be asked to make changes — that's normal!

## Development setup

1. Fork & clone the repo.
2. Open it in **Android Studio** (a version supporting AGP 9.x / Gradle 9.3).
3. Make sure you have **JDK 17+** and the **Android SDK 36** installed.
4. Let Gradle sync, then run the app on a device or emulator (API 26+).

### Command-line builds

```bash
./gradlew assembleDebug     # build debug APK
./gradlew test              # run unit tests
./gradlew lint              # run Android lint
```

> `org.gradle.java.home` is intentionally **not** committed (it is machine-specific). Gradle will use the JDK from `JAVA_HOME`/`PATH`. Pin a JDK locally via `~/.gradle/gradle.properties` if you need to.

## Code style

- Follow the existing **Kotlin official code style** (`kotlin.code.style=official`).
- Keep the **MVVM + Hilt** architecture: UI in Compose, logic in ViewModels, data via repositories/Room.
- One feature/fix per PR — keep changes focused.
- Add/update tests where reasonable.

## Branch naming

Use a short, descriptive prefix:

- `feature/...` — new features
- `fix/...` — bug fixes
- `chore/...` — build/config/docs
- `docs/...` — documentation only

## Commit messages

Write clear commit messages in the imperative mood, e.g. `Add RSS feed refresh on pull-to-refresh`.

## Important rules

- **Never commit secrets**, keystores, or signing files. The repo must stay buildable without private credentials.
- **Never commit the release signing key.** The Play Store signing key is private and is not part of this repository.
- Don't add analytics/tracking/ads — the app is intentionally ad-free and tracker-free.

Thanks for helping make Linkzary better! 💙
