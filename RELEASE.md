# Release Workflow

This document describes how to publish a new version of Linkzary — both the
Google Play Store build (AAB) and the GitHub Release build (APK).

> **Signing key:** The release signing keystore is **NOT** in this repository
> (it is gitignored). Only the maintainer holds it. Without it, anyone can build
> a debug APK, but only the maintainer can produce a Play-Store-compatible
> signed release.

## Prerequisites (one-time setup)

1. Have your release keystore (`.jks`) file on your machine.
2. Create a file named `keystore.properties` **at the project root** (next to
   `gradle.properties`). This file is gitignored and must never be committed.
   Use `keystore.properties.example` as a template:

   ```properties
   storeFile=/absolute/path/to/your/release-key.jks
   storePassword=your-store-password
   keyAlias=your-key-alias
   keyPassword=your-key-password
   ```

   `storeFile` may be an absolute path or relative to the project root.

3. When `keystore.properties` exists, the `release` build type is signed
   automatically using these credentials. Without it, release builds are
   unsigned (fine for contributors; not for publishing).

## 1. Prepare the release

1. Make sure `master` is up to date and CI is green.
2. Bump the version in `app/build.gradle.kts`:
   - `versionCode` — increment by 1 every release (integer, must always increase)
   - `versionName` — human-readable, e.g. `"1.2.0"`
3. Commit the version bump, e.g. `Bump version to 1.2.0`.
4. Create and push a git tag matching the version:

   ```bash
   git tag v1.2.0
   git push origin v1.2.0
   ```

## 2. Build the Play Store AAB

Google Play requires **Android App Bundles (.aab)**, not APKs.

```bash
./gradlew :app:bundleRelease
```

The signed AAB will be at:

```
app/build/outputs/bundle/release/app-release.aab
```

> If you use Play App Signing (recommended), Google holds your app signing key
> and you sign with your **upload key**. The AAB you upload is signed with the
> upload key; Google re-signs for distribution. Keep your upload keystore safe —
> losing it means you must contact Play Console support to reset it.

### Upload to Play Console

1. Go to https://play.google.com/console → your app → **Production** (or
   internal testing first) → **Create new release**.
2. Upload `app-release.aab`.
3. Add release notes, then **Review release** → **Start rollout**.

## 3. Build the GitHub Release APK

For GitHub Releases, distribute an **APK** (so users can sideload without
Google Play).

```bash
./gradlew :app:assembleRelease
```

The signed APK will be at:

```
app/build/outputs/apk/release/app-release.apk
```

> If `keystore.properties` is absent, the APK/AAB will be *unsigned* and named
> `app-release-unsigned.apk`. Make sure `keystore.properties` is present before
> building a release you intend to publish.

### Create the GitHub Release

1. Go to https://github.com/ZaryabKhan/Linkzary/releases/new
2. **Choose a tag** → select the tag you pushed (e.g. `v1.2.0`).
3. **Release title:** `v1.2.0`
4. **Description:** summarize changes (you can use `git log v1.1.5..v1.2.0
   --oneline` to list commits since the last release).
5. **Attach binaries:** drag in `app-release.apk`.
6. Check **"Set as the latest release"**.
7. **Publish release**.

## Quick reference (every release)

```bash
# 1. Bump versionCode/versionName in app/build.gradle.kts, commit & push to master
# 2. Tag and push the tag
git tag v1.2.0
git push origin v1.2.0

# 3. Build both artifacts (requires keystore.properties present)
./gradlew :app:bundleRelease :app:assembleRelease

# 4. Upload AAB to Play Console; attach APK to the GitHub Release for the tag
```

## Which artifact goes where?

| Target      | Format | Gradle task          | Output path                                          |
|-------------|--------|----------------------|------------------------------------------------------|
| Play Store  | `.aab` | `:app:bundleRelease`   | `app/build/outputs/bundle/release/app-release.aab`   |
| GitHub      | `.apk` | `:app:assembleRelease` | `app/build/outputs/apk/release/app-release.apk`      |
| Debug (dev) | `.apk` | `:app:assembleDebug`   | `app/build/outputs/apk/debug/app-debug.apk`          |
